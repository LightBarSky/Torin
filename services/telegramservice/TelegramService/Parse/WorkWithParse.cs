using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Infrastructure;
using Contracts.Parse;
using DTOs.DTO;
using TelegramService.Infrastructure;
using TelegramService.Infrastructure.ClassesForWrite;
using TelegramService.Parse.Fillers;
using TelegramService.Setting;
using TL;

namespace TelegramService.Parse
{
    interface IWorkWithParse
    {
        Task Parse();
    }
    internal class WorkWithParse : IWorkWithParse
    {
        private ParseTelegramCore _parseTelegramCore;
        private ClientBase _clientBase => _parseTelegramCore.ClientBase;
        private CancellationToken _cancellationToken => _parseTelegramCore.CancellationToken;
        private ILogger _logger => _parseTelegramCore.Logger;
        private ApiClient _api => _parseTelegramCore.ApiClient;
        private ParseGroupOptions _parseOptions => _parseTelegramCore.ParseOptions;

        private IWriters _writers;
        private IWorkWithTaskChats _workWithTaskChats;
        private IWorkWithGroups _workWithGroups;

        public WorkWithParse(ParseTelegramCore parseTelegramCore, IWriters writers, IWorkWithTaskChats workWithTaskChats, IWorkWithGroups workWithGroups)
        {
            _parseTelegramCore = parseTelegramCore;
            _writers = writers;
            _workWithTaskChats = workWithTaskChats;
            _workWithGroups = workWithGroups;
        }

        public async Task Parse()
        {
            while (!_cancellationToken.IsCancellationRequested)
            {
                try
                {
                    if (_parseTelegramCore.TryDequeue(out ChatBase? chatBase) && chatBase is not null)
                    {
                        _logger.Log(new Log()
                        {
                            Message = $"Канал/группа {chatBase.Title} поступила на обработку.",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Info
                        });
                        var task_ch = await _api.GetTaskChatByIdChat(chatBase.ID);

                        if (task_ch is null)
                        {
                            task_ch = new TaskChatDTO()
                            {
                                IdChat = chatBase.ID,
                                OffsetIdNewMessage = -1,
                                OffsetIdOldMessage = -1
                            };
                            await _api.PostTaskChat(task_ch);
                        }
                        List<Task> tasks = new();
                        if (_parseTelegramCore.isParseMessage()!)
                        {
                            tasks.Add(ParseMessage(chatBase));
                        }
                        if ((bool)_parseOptions.ParseUser!)
                        {
                            tasks.Add(ParseUserAsync(chatBase));
                        }
                        if ((bool)_parseOptions.ParseGifts!)
                        {
                            tasks.Add(ParseGifts(chatBase));
                        }
                        await Task.WhenAll(tasks);
                        _logger.Log(new Log()
                        {
                            Message = $"Канал/группа {chatBase.Title} обработана.",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Info
                        });
                        var group = await _api.GetGroupByIdGroup(chatBase.ID);
                        if (group is not null)
                        {
                            await _api.PatchGroupLastHandle((long)group.Id!, DateTime.UtcNow);
                        }
                        if (!_parseTelegramCore.ContainsDetectChannelPrivate(chatBase.ID))
                        {
                            _parseTelegramCore.AddChatQueue(chatBase);
                        }
                    }
                    if (chatBase is null)
                    {
                        await Task.Delay(5_000, cancellationToken: _cancellationToken);
                    }
                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = "Func [Parse]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                }
            }
        }

        private async Task ParseGifts(ChatBase chatBase)
        {
            try
            {
                string hash = string.Empty;
                for (; ; )
                {
                    Payments_SavedStarGifts? gg = null;
                    while (!_cancellationToken.IsCancellationRequested)
                    {
                        try
                        {
                            gg = await _clientBase.GetPayments_SavedStarGifts(chatBase: chatBase, hash: hash);
                            await Task.Delay((int)_parseOptions.DelayParseGiftsMS!, cancellationToken: _cancellationToken);
                            break;
                        }
                        catch (RpcException rpcException)
                        {
                            if (rpcException.Message == "CHAT_ADMIN_REQUIRED" || rpcException.Message == "CHANNEL_PRIVATE" ||
                            rpcException.Message == "CHANNEL_MONOFORUM_UNSUPPORTED")
                            {
                                break;
                            }
                            _logger.Log(new Log()
                            {
                                Message = $"[ParseGifts] RPCException [GetSavedStarGifts] [code {rpcException.Code}] [message: {rpcException.Message}]",
                                LogDT = DateTime.UtcNow,
                                Level = Levels.Error
                            });
                        }
                        catch (Exception ex)
                        {
                            _logger.Log(new Log()
                            {
                                Message = "Func [ParseGifts:GetSavedStarGifts]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                                LogDT = DateTime.UtcNow,
                                Level = Levels.Error
                            });
                            break;
                        }
                        await Task.Delay(5_000, cancellationToken: _cancellationToken);
                    }
                    if (gg is not null)
                    {
                        if (gg.users.Count > 0)
                        {
                            int batchSize = (int)_parseOptions.BatchForUser!;
                            for (int i = 0; i <= gg.users.Count / batchSize; i++)
                            {
                                if (_cancellationToken.IsCancellationRequested) return;
                                var partParticipants = gg.users.Skip(i * batchSize).Take(batchSize).ToList();
                                await _writers.WriteUserAndChatAsync(partParticipants.Select(x => new UserAndFullUser(x.Value, null)).ToList(), idChat: chatBase.ID);
                            }
                        }
                        if (gg.count > 0)
                        {
                            List<GiftsDTO> giftsDto = new List<GiftsDTO>(gg.count);
                            foreach (var g in gg.gifts)
                            {
                                var gift = new GiftsDTO()
                                {
                                    IdGroup = chatBase.ID,
                                    IdFrom = (g.from_id == null) ? 0 : g.from_id.ID,
                                    Message = g.message?.text,
                                    ConvertStars = g.convert_stars,
                                    Flags = g.flags.ToString(),
                                    Date = g.date
                                };

                                if (g.gift is StarGift sg)
                                {
                                    gift.Stars = sg.stars;
                                    gift.IdGift = sg.ID.ToString();
                                    gift.TitleGift = sg.title;
                                    gift.AvailabilityTotal = sg.AvailabilityTotal;
                                    gift.Flags2 = sg.flags.ToString();
                                }
                                giftsDto.Add(gift);
                            }
                            await _writers.WriteGifts(giftsDto, chatBase.ID);
                        }
                        if (gg.flags.HasFlag(TL.Payments_SavedStarGifts.Flags.has_next_offset))
                        {
                            hash = gg.next_offset;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else
                    {
                        break;
                    }
                }

            }
            catch (Exception ex)
            {
                _logger.Log(new Log()
                {
                    Message = "Func [ParseGifts]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Error
                });
            }
        }

        private async Task ParseMessage(ChatBase chat)
        {
            try
            {
                long newMessageOffset = await _workWithTaskChats.GetNewOffsetMessage(chat.ID);
                long oldMessageOffset = await _workWithTaskChats.GetOldOffsetMessage(chat.ID);
                if (newMessageOffset == -1 && oldMessageOffset != -1)
                {
                    newMessageOffset = oldMessageOffset;
                }
                if (newMessageOffset != -1 && oldMessageOffset == -1)
                {
                    oldMessageOffset = newMessageOffset;
                }
                long maxMessageId = newMessageOffset;
                bool parseNewEndFlag = (newMessageOffset == -1 && oldMessageOffset == -1) ? true : false;
                bool offsetFlag = false;
                bool joinGroupReplyFlag = false;
                bool flagExitDueToRubikonDate = false;
                HashSet<long> idsSkip = new();
                HashSet<long> userWithMinFlag = new();
                ParseMessageStorage storageChat = new(100);
                ParseMessageStorage storageChannel = new(100);
                DateTime rubikonDate = DateTime.UtcNow.AddDays(-(int)_parseOptions.ParseMessageRubikonDays!);
                DateTime offsetDate = DateTime.UtcNow.AddDays(-(int)_parseOptions.DaysDelayMessage!);
                if (chat.IsGroup)
                {
                    int count = 0;
                    int count_min = 0;
                    for (int offset_id = 0; ;)
                    {
                        if (_cancellationToken.IsCancellationRequested) return;
                        if (flagExitDueToRubikonDate || (parseNewEndFlag && (count >= _parseOptions.CountMessagesParseOnChat)))
                        {
                            _logger.Log(new Log()
                            {
                                Message = $"В группе {chat.Title} собрано сообщений [{count}]",
                                LogDT = DateTime.UtcNow,
                                Level = Levels.Info
                            });
                            break;
                        }
                        Messages_MessagesBase? messagesBase = null;
                        while (!_cancellationToken.IsCancellationRequested)
                        {
                            try
                            {
                                _logger.Log(new Log()
                                {
                                    Message = $"Начался сбор сообщений группы [id: {chat.ID}, title: {chat.Title}] [offset_id: {offset_id}]",
                                    LogDT = DateTime.UtcNow,
                                    Level = Levels.Info
                                });
                                messagesBase = await _clientBase.GetMessages_History(chat, offset_id);
                                _logger.Log(new Log()
                                {
                                    Message = $"Сбор сообщений группы окончен [title: {chat.Title}] [count: {messagesBase?.Messages.Length}]",
                                    LogDT = DateTime.UtcNow,
                                    Level = Levels.Info
                                });
                                await Task.Delay((int)_parseOptions.DelayParseMessageMS!, cancellationToken: _cancellationToken);
                                break;
                            }
                            catch (RpcException rpcException)
                            {
                                _logger.Log(new Log()
                                {
                                    Message = $"RPCException [ParseMessage:GetHistory:Group] [code {rpcException.Code}] [message: {rpcException.Message}]",
                                    LogDT = DateTime.UtcNow,
                                    Level = Levels.Error
                                });

                                if (rpcException.Message == "CHANNEL_PRIVATE")
                                {
                                    if (chat is TL.Channel channel)
                                    {
                                        var stat = await _workWithGroups.JoinChannel(channel);
                                        if (stat == "INVITE_REQUEST_SENT")
                                        {
                                            await _workWithGroups.RequestSentFuncAsync(channel.ID);
                                        }
                                        else if (stat != "JOINED")
                                        {
                                            await _workWithGroups.DetectChannelPrivate(channel.ID);
                                        }
                                    }
                                    else
                                    {
                                        await _workWithGroups.DetectChannelPrivate(chat.ID);
                                    }
                                    break;
                                }
                            }
                            catch (Exception ex)
                            {
                                _logger.Log(new Log()
                                {
                                    Message = "Func [ParseMessage:GetHistory:Group]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                                    LogDT = DateTime.UtcNow,
                                    Level = Levels.Error
                                });
                                break;
                            }
                            await Task.Delay(5_000, cancellationToken: _cancellationToken);
                        }

                        if (messagesBase is null || messagesBase?.Messages.Length == 0)
                        {
                            if (messagesBase?.Messages.Length == 0)
                            {
                                if (newMessageOffset < maxMessageId)
                                {
                                    await _writers.UpdateTaskChatAsync([new TaskChatDTO()
                                {
                                    IdChat = chat.ID,
                                    OffsetIdNewMessage = maxMessageId
                                }], chat.ID);
                                }
                                if (offset_id != 0)
                                {
                                    await _writers.UpdateTaskChatAsync([new TaskChatDTO()
                                {
                                    IdChat = chat.ID,
                                    OffsetIdOldMessage = offset_id
                                }], chat.ID);
                                }
                            }
                            break;
                        }

                        _logger.Log(new Log()
                        {
                            Message = $"Сбор пользователей начат!",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Info
                        });

                        if ((bool)_parseOptions.ParseMessageOnUsers! && messagesBase is Messages_ChannelMessages messages && messages.users is not null)
                        {
                            foreach (var user in messages.users.Values)
                            {
                                if (user is null) continue;
                                if (!idsSkip.Contains(user.ID))
                                {
                                    idsSkip.Add(user.ID);
                                    if (user.flags.HasFlag(TL.User.Flags.min))
                                    {
                                        userWithMinFlag.Add(user.ID);
                                    }
                                    else
                                    {
                                        storageChat.sendParticipant.Add(new UserAndFullUser(user, null));
                                    }
                                }
                            }

                            _logger.Log(new Log()
                            {
                                Message = $"В группе собрано без min-флага [{storageChat.sendParticipant.Count}/{messages.users.Count}] [idsSkip: {idsSkip.Count}]",
                                LogDT = DateTime.UtcNow,
                                Level = Levels.Info
                            });
                        }

                        int handle_messages = 0;
                        int skip_messages = 0;
                        foreach (var msgBase in messagesBase!.Messages)
                        {
                            if (_cancellationToken.IsCancellationRequested) return;
                            if (msgBase is null)
                            {
                                handle_messages++;
                                continue;
                            }
                            if (!parseNewEndFlag && msgBase.ID <= newMessageOffset)
                            {
                                offsetFlag = true;
                                parseNewEndFlag = true;
                                await _writers.UpdateTaskChatAsync([new TaskChatDTO()
                            {
                                IdChat = chat.ID,
                                OffsetIdNewMessage = maxMessageId
                            }], chat.ID);

                                newMessageOffset = maxMessageId;
                                break;
                            }
                            //вот что добавил для отмены по пределу даты
                            if (msgBase.Date < rubikonDate)
                            {
                                flagExitDueToRubikonDate = true;
                                break;
                            }
                            //*******************************************

                            //вот что добавил по offsetDate
                            if (msgBase.Date > offsetDate)
                            {
                                skip_messages++;
                                continue;
                            }
                            //*******************************************

                            if (msgBase is TL.Message msg)
                            {
                                //string? mediaId = null;
                                long? replyTo = null;
                                if ((bool)_parseOptions.ParseMessageOnReactions!)
                                {
                                    ParseMessageFiller.FillerReactions(msg, chat.ID, ref storageChat);
                                }
                                if ((bool)_parseOptions.ParseMessageOnReactionsGeneral!)
                                {
                                    ParseMessageFiller.FillerReactionsGeneral(msg, chat.ID, ref storageChat, 2);
                                }
                                if ((bool)_parseOptions.ParseMessageOnEntities!)
                                {
                                    ParseMessageFiller.FillerEntities(msg, chat.ID, ref storageChat, 2);
                                }
                                if ((bool)_parseOptions.ParseMessageOnProperties!)
                                {
                                    storageChat.sendMessagesProperties.Add(ParseMessageFiller.FillerProperties(msg, chat.ID, 2));
                                }


                                if (msg.flags.HasFlag(Message.Flags.has_reply_to))
                                {
                                    var ReplyTo = msg.ReplyTo as MessageReplyHeader;
                                    if (ReplyTo != null)
                                        replyTo = ReplyTo.reply_to_msg_id;
                                }
                                if ((bool)_parseOptions.ParseMessageOnMessages! && !string.IsNullOrEmpty(msg.message))
                                {
                                    storageChat.sendMessage.Add(new MessageDTO()
                                    {
                                        IdGroup = chat.ID,
                                        IsComments = 2,
                                        IdGroupedMessage = msg.flags.HasFlag(Message.Flags.has_grouped_id) ? msg.grouped_id : null,
                                        IdMessage = msg.ID,
                                        IdUser = msg.From?.ID,
                                        ContentText = msg.message,
                                        IdReply = replyTo,
                                        Date = msg.Date
                                    });
                                }

                            }
                            if (msgBase.From is not null && userWithMinFlag.Contains(msgBase.From.ID))
                            {
                                Users_UserFull? us_full_gr = null;
                                InputUserFromMessage inputUserFromMessage_gr = new()
                                {
                                    msg_id = msgBase.ID,
                                    peer = messagesBase!.UserOrChat(msgBase.Peer).ToInputPeer(),
                                    user_id = msgBase.From.ID
                                };

                                while (!_cancellationToken.IsCancellationRequested)
                                {
                                    try
                                    {
                                        us_full_gr = await _clientBase.GetFullUser(inputUserFromMessage_gr);
                                        break;
                                    }
                                    catch (RpcException rpcException)
                                    {
                                        _logger.Log(new Log()
                                        {
                                            Message = $"RPCException [ParseMessage:GetFullUser:Group] [code {rpcException.Code}] [message: {rpcException.Message}]",
                                            LogDT = DateTime.UtcNow,
                                            Level = Levels.Error
                                        });

                                        if (rpcException.Message == "CHANNEL_PRIVATE" || rpcException.Message == "MSG_ID_INVALID" ||
                                                        rpcException.Message == "PEER_ID_INVALID")
                                        {
                                            userWithMinFlag.Remove(msgBase.From.ID);
                                            break;
                                        }
                                    }
                                    catch (Exception ex)
                                    {
                                        _logger.Log(new Log()
                                        {
                                            Message = "Func [ParseMessage:GetFullUser:Group]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                                            LogDT = DateTime.UtcNow,
                                            Level = Levels.Error
                                        });
                                        break;
                                    }
                                    await Task.Delay(1000, cancellationToken: _cancellationToken);
                                }
                                if (us_full_gr != null)
                                {
                                    if (us_full_gr.users.Count > 0)
                                    {
                                        storageChat.sendParticipant.Add(new UserAndFullUser(us_full_gr.users.First().Value, us_full_gr.full_user));
                                        userWithMinFlag.Remove(msgBase.From.ID);
                                        count_min++;
                                    }
                                }
                            }
                            handle_messages++;
                        }

                        await _writers.WriterParseMessage(storageChat, chat.ID);
                        storageChat.ClearStorage();
                        if (handle_messages > 0)
                        {
                            _logger.Log(new Log()
                            {
                                Message = $"Обработано [handle_posts = {handle_messages}], пропущено [skip_messages = {skip_messages}]",
                                LogDT = DateTime.UtcNow,
                                Level = Levels.Info
                            });
                            count += handle_messages;
                            int idMesMin = messagesBase.Messages.Skip(skip_messages).Take(handle_messages).Min(x => x.ID);
                            int idMesMax = messagesBase.Messages.Skip(skip_messages).Take(handle_messages).Max(x => x.ID);
                            maxMessageId = Math.Max(maxMessageId, idMesMax);
                            if (oldMessageOffset == -1 && newMessageOffset == -1)
                            {
                                await _writers.UpdateTaskChatAsync([new TaskChatDTO()
                        {
                            IdChat = chat.ID,
                            OffsetIdOldMessage = idMesMin,
                            OffsetIdNewMessage = idMesMax
                        }], chat.ID);
                                oldMessageOffset = idMesMin;
                                newMessageOffset = idMesMax;
                            }
                            else
                            {
                                if (oldMessageOffset > idMesMin)
                                {
                                    await _writers.UpdateTaskChatAsync([new TaskChatDTO()
                            {
                                IdChat = chat.ID,
                                OffsetIdOldMessage = idMesMin
                            }], chat.ID);
                                    oldMessageOffset = idMesMin;
                                }
                            }
                        }


                        if (offsetFlag)
                        {
                            if (offset_id != oldMessageOffset)
                                offset_id = (int)oldMessageOffset;
                            offsetFlag = false;
                        }
                        else
                        {
                            offset_id = messagesBase.Messages[^1].ID;
                        }
                        //*********************************************
                        if (flagExitDueToRubikonDate)
                        {
                            if (newMessageOffset < maxMessageId)
                            {
                                await _writers.UpdateTaskChatAsync([new TaskChatDTO()
                                {
                                    IdChat = chat.ID,
                                    OffsetIdNewMessage = maxMessageId
                                }], chat.ID);
                            }
                        }
                        //*********************************************
                        _logger.Log(new Log()
                        {
                            Message = $"В группе обработано с min-флагом [{count_min}]",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Info
                        });
                        _logger.Log(new Log()
                        {
                            Message = $"В группе {chat.Title} собрано [{count}/{messagesBase.Count}]",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Info
                        });
                    }
                }
                else
                {
                    int count_message_all = 0;
                    int count_post = 0;
                    int count_min = 0;
                    bool flagExit = false;
                    for (int offset_id = 0; ;)
                    {
                        if (_cancellationToken.IsCancellationRequested) return;
                        if (flagExitDueToRubikonDate || flagExit)
                        {
                            _logger.Log(new Log()
                            {
                                Message = $"В канала [title: {chat.Title}] собрано постов [{count_post}], комментариев и постов [{count_message_all}]",
                                LogDT = DateTime.UtcNow,
                                Level = Levels.Info
                            });
                            break;
                        }
                        Messages_MessagesBase? messagesBase = null;
                        while (!_cancellationToken.IsCancellationRequested)
                        {
                            try
                            {
                                _logger.Log(new Log()
                                {
                                    Message = $"Начался сбор постов канала [id: {chat.ID}, title: {chat.Title}] [offset_id: {offset_id}]",
                                    LogDT = DateTime.UtcNow,
                                    Level = Levels.Info
                                });
                                messagesBase = await _clientBase.GetMessages_History(chat,
                                offset_id: offset_id);
                                _logger.Log(new Log()
                                {
                                    Message = $"Сбор постов канала окончен [id: {chat.ID}, title: {chat.Title}] [count: {messagesBase?.Messages.Length}]",
                                    LogDT = DateTime.UtcNow,
                                    Level = Levels.Info
                                });
                                await Task.Delay((int)_parseOptions.DelayParseMessageMS!, cancellationToken: _cancellationToken);
                                break;
                            }
                            catch (RpcException rpcException)
                            {
                                _logger.Log(new Log()
                                {
                                    Message = $"RPCException [ParseMessage:GetHistory:Channel] [code {rpcException.Code}] [message: {rpcException.Message}]",
                                    LogDT = DateTime.UtcNow,
                                    Level = Levels.Error
                                });

                                if (rpcException.Message == "CHANNEL_PRIVATE")
                                {
                                    if (chat is TL.Channel channel)
                                    {
                                        var stat = await _workWithGroups.JoinChannel(channel);
                                        if (stat == "INVITE_REQUEST_SENT")
                                        {
                                            await _workWithGroups.RequestSentFuncAsync(channel.ID);
                                        }
                                        else if (stat != "JOINED")
                                        {
                                            await _workWithGroups.DetectChannelPrivate(channel.ID);
                                        }
                                    }
                                    else
                                    {
                                        await _workWithGroups.DetectChannelPrivate(chat.ID);
                                    }
                                    break;
                                }
                            }
                            catch (Exception ex)
                            {
                                _logger.Log(new Log()
                                {
                                    Message = "Func [ParseMessage:GetHistory:Channel]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                                    LogDT = DateTime.UtcNow,
                                    Level = Levels.Error
                                });
                                break;
                            }
                            await Task.Delay(5_000, cancellationToken: _cancellationToken);
                        }

                        if (messagesBase is null || messagesBase?.Messages.Length == 0)
                        {
                            if (messagesBase?.Messages.Length == 0)
                            {
                                if (newMessageOffset < maxMessageId)
                                {
                                    await _writers.UpdateTaskChatAsync([new TaskChatDTO()
                                {
                                    IdChat = chat.ID,
                                    OffsetIdNewMessage = maxMessageId
                                }], chat.ID);
                                }
                                if (offset_id != 0)
                                {
                                    await _writers.UpdateTaskChatAsync([new TaskChatDTO()
                                {
                                    IdChat = chat.ID,
                                    OffsetIdOldMessage = offset_id
                                }], chat.ID);
                                }
                            }
                            break;
                        }

                        int handle_posts = 0;
                        int skip_messages = 0;
                        foreach (var msgBase in messagesBase!.Messages)
                        {
                            if (_cancellationToken.IsCancellationRequested) return;
                            if (parseNewEndFlag && (count_message_all >= _parseOptions.CountMessagesParseOnChat))
                            {
                                flagExit = true;
                                break;
                            }
                            if (msgBase is null)
                            {
                                handle_posts++;
                                continue;
                            }
                            if (!parseNewEndFlag && msgBase.ID <= newMessageOffset)
                            {
                                offsetFlag = true;
                                parseNewEndFlag = true;
                                await _writers.UpdateTaskChatAsync([new TaskChatDTO()
                            {
                                IdChat = chat.ID,
                                OffsetIdNewMessage = maxMessageId
                            }], chat.ID);

                                newMessageOffset = maxMessageId;
                                break;
                            }
                            //вот что добавил для отмены по пределу даты
                            if (msgBase.Date < rubikonDate)
                            {
                                flagExitDueToRubikonDate = true;
                                break;
                            }
                            //*******************************************

                            //вот что добавил по offsetDate
                            if (msgBase.Date > offsetDate)
                            {
                                skip_messages++;
                                continue;
                            }
                            //*******************************************

                            if (msgBase is TL.Message msg)
                            {
                                //string? mediaId = null;
                                long? replyTo = null;
                                if ((bool)_parseOptions.ParseMessageOnProperties!)
                                {
                                    storageChannel.sendMessagesProperties.Add(ParseMessageFiller.FillerProperties(msg, chat.ID, 0));
                                }
                                if ((bool)_parseOptions.ParseMessageOnReactionsGeneral!)
                                {
                                    ParseMessageFiller.FillerReactionsGeneral(msg, chat.ID, ref storageChannel, 0);
                                }
                                if ((bool)_parseOptions.ParseMessageOnEntities!)
                                {
                                    ParseMessageFiller.FillerEntities(msg, chat.ID, ref storageChannel, 0);
                                }

                                if (msg.flags.HasFlag(Message.Flags.has_reply_to) || msg.ReplyTo is not null)
                                {
                                    var ReplyTo = msg.ReplyTo as MessageReplyHeader;
                                    if (ReplyTo != null)
                                    {
                                        replyTo = ReplyTo.reply_to_msg_id;
                                    }
                                }
                                if ((bool)_parseOptions.ParseMessageOnMessages! && !string.IsNullOrEmpty(msg.message))
                                {
                                    storageChannel.sendMessage.Add(new MessageDTO()
                                    {
                                        IdGroup = chat.ID,
                                        IsComments = 0,
                                        IdGroupedMessage = msg.flags.HasFlag(Message.Flags.has_grouped_id) ? msg.grouped_id : null,
                                        IdMessage = msg.ID,
                                        IdUser = null,
                                        ContentText = msg.message,
                                        IdReply = replyTo,
                                        Date = msg.Date
                                    });
                                }

                                if (msg.replies?.replies is not null && msg.replies?.replies > 0)
                                {
                                    int count_replies = 0;
                                    for (int offset_id_reply = 0; ;)
                                    {
                                        if (_cancellationToken.IsCancellationRequested) return;
                                        Messages_MessagesBase? replies = null;
                                        while (!_cancellationToken.IsCancellationRequested)
                                        {
                                            try
                                            {
                                                _logger.Log(new Log()
                                                {
                                                    Message = $"Начался сбор комментариев канала [id: {chat.ID}, title: {chat.Title}] [post: {msg.ID}] [offset_id: {offset_id}]",
                                                    LogDT = DateTime.UtcNow,
                                                    Level = Levels.Info
                                                });
                                                replies = await _clientBase.GetReplies(peer: chat, msg_id: msg.ID, offset_id: offset_id_reply);
                                                _logger.Log(new Log()
                                                {
                                                    Message = $"Сбор комментариев канала окончен [id: {chat.ID}, title: {chat.Title}] [count: {replies?.Messages?.Length}]",
                                                    LogDT = DateTime.UtcNow,
                                                    Level = Levels.Info
                                                });
                                                await Task.Delay((int)_parseOptions.DelayParseRepliesMS!, cancellationToken: _cancellationToken);
                                                break;
                                            }
                                            catch (RpcException rpcException)
                                            {
                                                _logger.Log(new Log()
                                                {
                                                    Message = $"RPCException [ParseMessage:GetReplies:Channel] [code {rpcException.Code}] [message: {rpcException.Message}]",
                                                    LogDT = DateTime.UtcNow,
                                                    Level = Levels.Error
                                                });

                                                if (rpcException.Message == "CHANNEL_PRIVATE" || rpcException.Message == "MSG_ID_INVALID")
                                                {
                                                    break;
                                                }
                                            }
                                            catch (Exception ex)
                                            {
                                                _logger.Log(new Log()
                                                {
                                                    Message = "Func [ParseMessage:GetReplies:Channel]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                                                    LogDT = DateTime.UtcNow,
                                                    Level = Levels.Error
                                                });
                                                break;
                                            }
                                            await Task.Delay(10_000, cancellationToken: _cancellationToken);
                                        }

                                        if (replies is null || replies.Messages.Length == 0)
                                        {
                                            break;
                                        }

                                        count_replies += replies.Messages.Length;
                                        count_message_all += replies.Messages.Length;
                                        _logger.Log(new Log()
                                        {
                                            Message = $"Сбор пользователей начат [post: {msg.ID}]!",
                                            LogDT = DateTime.UtcNow,
                                            Level = Levels.Info
                                        });


                                        ChatBase? chatPeer = null;
                                        if ((bool)_parseOptions.ParseMessageOnUsers! && replies is Messages_ChannelMessages messages && messages.users is not null)
                                        {
                                            if (messages.chats is not null)
                                            {
                                                chatPeer = messages.chats.Select(x => x.Value).FirstOrDefault();
                                            }

                                            foreach (var user in messages.users.Values)
                                            {
                                                if (user is null) continue;
                                                if (!idsSkip.Contains(user.ID))
                                                {
                                                    idsSkip.Add(user.ID);
                                                    if (user.flags.HasFlag(TL.User.Flags.min))
                                                    {
                                                        userWithMinFlag.Add(user.ID);
                                                    }
                                                    else
                                                    {
                                                        storageChat.sendParticipant.Add(new UserAndFullUser(user, null));
                                                    }
                                                }
                                            }

                                            _logger.Log(new Log()
                                            {
                                                Message = $"В комментариях собрано без min-флага [{storageChat.sendParticipant.Count}/{messages.users.Count}] [idsSkip: {idsSkip.Count}]",
                                                LogDT = DateTime.UtcNow,
                                                Level = Levels.Info
                                            });
                                        }
                                        foreach (var messageBaseReply in replies.Messages)
                                        {
                                            if (_cancellationToken.IsCancellationRequested) return;

                                            if (messageBaseReply is TL.Message msgReply)
                                            {
                                                //string? mediaId_reply = null;
                                                long? replyTo_reply = null;
                                                if ((bool)_parseOptions.ParseMessageOnReactions!)
                                                {
                                                    ParseMessageFiller.FillerReactions(msgReply, chat.ID, ref storageChat);
                                                }
                                                if ((bool)_parseOptions.ParseMessageOnReactionsGeneral!)
                                                {
                                                    ParseMessageFiller.FillerReactionsGeneral(msgReply, chat.ID, ref storageChat, 1);
                                                }
                                                if ((bool)_parseOptions.ParseMessageOnEntities!)
                                                {
                                                    ParseMessageFiller.FillerEntities(msgReply, chat.ID, ref storageChat, 1);
                                                }
                                                if ((bool)_parseOptions.ParseMessageOnProperties!)
                                                {
                                                    storageChat.sendMessagesProperties.Add(ParseMessageFiller.FillerProperties(msgReply, chat.ID, 1));
                                                }


                                                if (msgReply.flags.HasFlag(Message.Flags.has_reply_to) || msgReply.ReplyTo is not null)
                                                {
                                                    var ReplyTo = msgReply.ReplyTo as MessageReplyHeader;
                                                    if (ReplyTo is not null && ReplyTo.flags.HasFlag(MessageReplyHeader.Flags.has_reply_to_top_id))
                                                    {
                                                        replyTo_reply = ReplyTo.reply_to_msg_id;
                                                    }
                                                }
                                                if ((bool)_parseOptions.ParseMessageOnMessages! && !string.IsNullOrEmpty(msgReply.message))
                                                {
                                                    storageChat.sendMessage.Add(new MessageDTO()
                                                    {
                                                        IdGroup = chat.ID,
                                                        IsComments = 1,
                                                        ReplyToPost = msg.ID,
                                                        IdGroupedMessage = msgReply.flags.HasFlag(Message.Flags.has_grouped_id) ? msgReply.grouped_id : null,
                                                        IdMessage = msgReply.ID,
                                                        IdUser = msgReply.From?.ID,
                                                        ContentText = msgReply.message,
                                                        IdReply = replyTo_reply,
                                                        Date = msgReply.Date
                                                    });
                                                }

                                            }

                                            if (messageBaseReply.From is not null && userWithMinFlag.Contains(messageBaseReply.From.ID))
                                            {
                                                Users_UserFull? us_full_gr = null;
                                                InputUserFromMessage inputUserFromMessage_gr = new()
                                                {
                                                    msg_id = messageBaseReply.ID,
                                                    peer = chatPeer,
                                                    user_id = messageBaseReply.From.ID
                                                };

                                                while (!_cancellationToken.IsCancellationRequested)
                                                {
                                                    try
                                                    {
                                                        us_full_gr = await _clientBase.GetFullUser(inputUserFromMessage_gr);
                                                        break;
                                                    }
                                                    catch (RpcException rpcException)
                                                    {
                                                        _logger.Log(new Log()
                                                        {
                                                            Message = $"RPCException [ParseMessage:GetFullUser:Channel] [code {rpcException.Code}] [message: {rpcException.Message}]",
                                                            LogDT = DateTime.UtcNow,
                                                            Level = Levels.Error
                                                        });

                                                        if (rpcException.Message == "CHANNEL_PRIVATE" || rpcException.Message == "MSG_ID_INVALID" ||
                                                        rpcException.Message == "PEER_ID_INVALID")
                                                        {
                                                            if (rpcException.Message == "CHANNEL_PRIVATE" && !joinGroupReplyFlag)
                                                            {
                                                                if (chatPeer is TL.Channel channel)
                                                                {
                                                                    joinGroupReplyFlag = true;
                                                                    var stat = await _workWithGroups.JoinChannel(channel);
                                                                }
                                                            }
                                                            userWithMinFlag.Remove(messageBaseReply.From.ID);
                                                            break;
                                                        }
                                                    }
                                                    catch (Exception ex)
                                                    {
                                                        _logger.Log(new Log()
                                                        {
                                                            Message = "Func [ParseMessage:GetFullUser:Channel]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                                                            LogDT = DateTime.UtcNow,
                                                            Level = Levels.Error
                                                        });
                                                        break;
                                                    }
                                                    await Task.Delay(1_000, cancellationToken: _cancellationToken);
                                                }
                                                if (us_full_gr != null)
                                                {
                                                    if (us_full_gr.users.Count > 0)
                                                    {
                                                        storageChat.sendParticipant.Add(new UserAndFullUser(us_full_gr.users.First().Value, us_full_gr.full_user));
                                                        userWithMinFlag.Remove(messageBaseReply.From.ID);
                                                        count_min++;
                                                    }
                                                }
                                            }
                                        }
                                        await _writers.WriterParseMessage(storageChat, chat.ID);
                                        storageChat.ClearStorage();
                                        offset_id_reply = replies.Messages[^1].ID;
                                        _logger.Log(new Log()
                                        {
                                            Message = $"В комментариях обработано с min-флагом [{count_min}]",
                                            LogDT = DateTime.UtcNow,
                                            Level = Levels.Info
                                        });
                                        _logger.Log(new Log()
                                        {
                                            Message = $"Под постом [post: {msg.ID}] собрано [{count_replies}/{replies.Count}]",
                                            LogDT = DateTime.UtcNow,
                                            Level = Levels.Info
                                        });
                                    }
                                }
                            }
                            handle_posts++;
                            count_post++;
                        }
                        await _writers.WriterParseMessage(storageChannel, chat.ID);
                        storageChannel.ClearStorage();
                        if (handle_posts > 0)
                        {
                            _logger.Log(new Log()
                            {
                                Message = $"Обработано [handle_posts = {handle_posts}], пропущено [skip_messages = {skip_messages}]",
                                LogDT = DateTime.UtcNow,
                                Level = Levels.Info
                            });
                            count_message_all += handle_posts;
                            int idMesMin = messagesBase.Messages.Skip(skip_messages).Take(handle_posts).Min(x => x.ID);
                            int idMesMax = messagesBase.Messages.Skip(skip_messages).Take(handle_posts).Max(x => x.ID);

                            maxMessageId = Math.Max(maxMessageId, idMesMax);
                            if (oldMessageOffset == -1 && newMessageOffset == -1)
                            {
                                await _writers.UpdateTaskChatAsync([new TaskChatDTO()
                            {
                                IdChat = chat.ID,
                                OffsetIdOldMessage = idMesMin,
                                OffsetIdNewMessage = idMesMax
                            }], chat.ID);
                                oldMessageOffset = idMesMin;
                                newMessageOffset = idMesMax;
                            }
                            else
                            {
                                if (oldMessageOffset > idMesMin)
                                {
                                    await _writers.UpdateTaskChatAsync([new TaskChatDTO()
                            {
                                IdChat = chat.ID,
                                OffsetIdOldMessage = idMesMin
                            }], chat.ID);
                                    oldMessageOffset = idMesMin;
                                }
                            }
                        }


                        if (offsetFlag)
                        {
                            if (offset_id != oldMessageOffset)
                                offset_id = (int)oldMessageOffset;
                            offsetFlag = false;
                        }
                        else
                        {
                            offset_id = messagesBase.Messages[^1].ID;
                        }

                        //*********************************************
                        if (flagExitDueToRubikonDate)
                        {
                            if (newMessageOffset < maxMessageId)
                            {
                                await _writers.UpdateTaskChatAsync([new TaskChatDTO()
                                {
                                    IdChat = chat.ID,
                                    OffsetIdNewMessage = maxMessageId
                                }], chat.ID);
                            }
                        }
                        //*********************************************

                        _logger.Log(new Log()
                        {
                            Message = $"В канале [title: {chat.Title}] собрано постов [{count_post}/{messagesBase.Count}], комментариев и постов [{count_message_all}]",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Info
                        });
                    }
                }


                _logger.Log(new Log()
                {
                    Message = $"Обработка завершена [title: {chat.Title}]",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Info
                });

            }
            catch (Exception ex)
            {
                _logger.Log(new Log()
                {
                    Message = "Func [ParseMessage]" + "\nError: " + ex.Message + "\n" + ex.ToString(),
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Error
                });
            }
        }

        private async Task ParseUserAsync(ChatBase chat)
        {
            try
            {
                var task_ch = await _api.GetTaskChatByIdChat(chat.ID);
                if (task_ch is not null && task_ch.DateParseUser.HasValue)
                {
                    var dateParseUser = task_ch.DateParseUser.Value;
                    dateParseUser = dateParseUser.AddDays((int)_parseOptions.DaysDelayParseUser!);
                    if (DateTime.UtcNow < dateParseUser)
                    {
                        _logger.Log(new Log()
                        {
                            Message = "[ParseUser] Не прошло 3 дней для парсинга пользователей чата/канала.",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Info
                        });
                        return;
                    }
                }
                Channels_ChannelParticipants? channelParticipants = null;
                var channel = chat as TL.Channel;
                if (channel is null)
                {
                    _logger.Log(new Log()
                    {
                        Message = $"[ParseUser] Чат/канал {chat.Title} не преобразован в TL.Channel",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Warning
                    });
                    return;
                }
                while (!_cancellationToken.IsCancellationRequested)
                {
                    try
                    {
                        channelParticipants = await _clientBase.GetAllParticipants(channel: channel, cancellationToken: _cancellationToken);
                        break;
                    }
                    catch (RpcException rpcException)
                    {
                        if (rpcException.Message == "CHAT_ADMIN_REQUIRED" || rpcException.Message == "CHANNEL_PRIVATE" ||
                        rpcException.Message == "CHANNEL_MONOFORUM_UNSUPPORTED")
                        {
                            break;
                        }
                        _logger.Log(new Log()
                        {
                            Message = $"[ParseUser] RPCException [ParseUser:GetAllParticipants] [code {rpcException.Code}] [message: {rpcException.Message}]",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Error
                        });
                    }
                    catch (Exception ex)
                    {
                        _logger.Log(new Log()
                        {
                            Message = $"[ParseUser] Exception [message: {ex.Message}]",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Error
                        });
                        break;
                    }
                    await Task.Delay(5_000, cancellationToken: _cancellationToken);
                }
                if (channelParticipants is not null)
                {
                    var idsAdmin = channelParticipants.participants.Where(x => x is TL.ChannelParticipantAdmin).Select(x => x.UserId).ToList();
                    var idsCreator = channelParticipants.participants.Where(x => x is TL.ChannelParticipantCreator).Select(x => x.UserId).ToList();
                    Dictionary<long, ChannelParticipant> participantsDict = channelParticipants.participants
                    .Where(x => x is TL.ChannelParticipant)
                    .Select(x => (ChannelParticipant)x)
                    .ToDictionary(key => key.UserId, val => val);
                    List<AdminChatDTO> adminChats = new();
                    foreach (var item in idsAdmin)
                    {
                        adminChats.Add(new AdminChatDTO()
                        {
                            IdUser = item,
                            IdGroup = channel.ID,
                            Status = "admin"
                        });
                    }
                    foreach (var item in idsCreator)
                    {
                        adminChats.Add(new AdminChatDTO()
                        {
                            IdUser = item,
                            IdGroup = channel.ID,
                            Status = "creator"
                        });
                    }

                    if (adminChats.Count > 0)
                    {
                        await _writers.WriteUserAdminOrCreatorAsync(adminChats, chat.ID);
                    }

                    int batchSize = (int)_parseOptions.BatchForUser!;
                    for (int i = 0; i <= channelParticipants.users.Count / batchSize; i++)
                    {
                        if (_cancellationToken.IsCancellationRequested) return;
                        var partParticipants = channelParticipants.users.Skip(i * batchSize).Take(batchSize).ToList();
                        await _writers.WriteUserAndChatAsync(partParticipants.Select(x => new UserAndFullUser(x.Value, null)).ToList(),
                        idChat: chat.ID, participants: participantsDict);
                    }
                }
                if (task_ch is not null)
                {
                    await _api.PatchTaskChatsParseUser(task_ch.IdChat, DateTime.UtcNow);
                }
            }
            catch (Exception ex)
            {
                _logger.Log(new Log()
                {
                    Message = "Func [ParseUser]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Error
                });
            }
        }
    }
}