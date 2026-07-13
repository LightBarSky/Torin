using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Infrastructure;
using Contracts.Parse;
using DTOs.DTO;
using TelegramService.Infrastructure;
using TelegramService.Infrastructure.ClassesForWrite;
using TelegramService.Setting;
using TL;

namespace TelegramService.Parse
{
    interface IWorkWithGroups
    {
        Task UpdateGroup();
        Task LoaderGroup();
        Task<string?> JoinChannel(TL.InputChannel inputChannel);
        Task DetectChannelPrivate(long idGroup, bool flagDisabled = false);
        Task RequestSentFuncAsync(long channelId);
    }

    internal class WorkWithGroups : IWorkWithGroups
    {
        private readonly ParseTelegramCore _parseTelegramCore;
        private ILogger _logger => _parseTelegramCore.Logger;
        private ApiClient _api => _parseTelegramCore.ApiClient;
        private ParseGroupOptions _parseOptions => _parseTelegramCore.ParseOptions;
        private ClientBase _clientBase => _parseTelegramCore.ClientBase;
        private long _handlerId => _parseTelegramCore.HandlerId;
        private TopicsKafka _topicsKafka => _parseTelegramCore.TopicsKafka;
        private Func<IKafkaMessageCreator, Task> _kafkaSendMessage => _parseTelegramCore.KafkaSendMessage;
        private CancellationToken _cancellationToken => _parseTelegramCore.CancellationToken;
        private Random _random = new Random();


        public WorkWithGroups(ParseTelegramCore parseTelegramCore)
        {
            _parseTelegramCore = parseTelegramCore;
        }

        private async Task SetSendRequestAsync(long? idGroup = null, string? hashGroup = null)
        {
            try
            {
                if (idGroup is not null)
                {
                    var group = await _api.GetGroupByIdGroup((long)idGroup);
                    if (group is not null)
                    {
                        group.TotalSendRequest += 1;
                        await _api.PatchGroupRecalculate(group);
                    }
                }
                else if (!string.IsNullOrEmpty(hashGroup))
                {
                    var group = await _api.GetGroupByHashAndHandlersId(hashGroup, _handlerId);
                    if (group is not null)
                    {
                        group.TotalSendRequest += 1;
                        await _api.PatchGroupRecalculate(group);
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.Log(new Log()
                {
                    Message = $"Func [SetSendRequest] [message: {ex.Message}] [innerException: {ex.InnerException?.Message}]",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Error
                });
            }
        }

        private async Task<long?> GetSendRequestAsync(long? idGroup = null, string? hashGroup = null)
        {
            long? result = null;
            try
            {
                if (idGroup is not null)
                {
                    var group = await _api.GetGroupByIdGroup((long)idGroup);
                    if (group is not null && group.TotalSendRequest > _parseOptions.TotalSendRequest)
                    {
                        result = group.TotalSendRequest;
                    }
                }
                else if (!string.IsNullOrEmpty(hashGroup))
                {
                    var group = await _api.GetGroupByHashAndHandlersId(hashGroup, _handlerId);
                    if (group is not null && group.TotalSendRequest > _parseOptions.TotalSendRequest)
                    {
                        result = group.TotalSendRequest;
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.Log(new Log()
                {
                    Message = $"Func [GetSendRequest] [message: {ex.Message}] [innerException: {ex.InnerException?.Message}]",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Error
                });
            }
            return result;
        }

        private async Task RemoveGroupAsync(WordGroupAllDTO group)
        {
            try
            {
                if (group.IdGroup is not null)
                {
                    group.HandlersId = -1;
                    await _api.PatchGroupRecalculate(group);
                }
                else
                {
                    await _api.DeleteGroupById((long)group.Id!);
                }
            }
            catch (Exception ex)
            {
                _logger.Log(new Log()
                {
                    Message = $"Func [RemoveGroupAsync] [message: {ex.Message}] [innerException: {ex.InnerException?.Message}]",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Error
                });
            }
        }

        private WordGroupAllDTO ChannelToWGA(TL.Channel channel, string? hashGroup = null, string? infoGroup = null, long? linked_id = null, long? participants_count = null)
        {
            return new WordGroupAllDTO()
            {
                IdGroup = channel.ID,
                FindGroup = channel.MainUsername,
                HashGroup = hashGroup,
                TitleGroup = channel.Title,
                LastUpdate = DateTime.UtcNow,
                Flags = channel.flags.ToString(),
                Flags2 = channel.flags2.ToString(),
                InfoGroup = infoGroup,
                ParticipantsCount = participants_count is null ? (channel.participants_count > 0 ? channel.participants_count : null) : participants_count,
                Type = channel.IsGroup ? 0 : 1,
                LinkedId = linked_id,
                CreatedDate = channel.date
            };
        }

        private WordGroupAllDTO ChatToWGA(TL.Chat chat, string? hashGroup = null, string? infoGroup = null, long? linked_id = null, long? participants_count = null)
        {
            return new WordGroupAllDTO()
            {
                IdGroup = chat.ID,
                FindGroup = chat.MainUsername,
                HashGroup = hashGroup,
                TitleGroup = chat.Title,
                InfoGroup = infoGroup,
                LastUpdate = DateTime.UtcNow,
                Flags = chat.flags.ToString(),
                ParticipantsCount = participants_count is null ? (chat.participants_count > 0 ? chat.participants_count : null) : participants_count,
                Type = chat.IsGroup ? 0 : 1,
                LinkedId = linked_id,
                CreatedDate = chat.date
            };
        }

        private async Task UpdateOldOffGroup(WordGroupAllDTO group, WordGroupAllDTO channelOrChat)
        {
            group.HandlersId = _handlerId;
            group.TotalDetectPrivate = 0;
            group.TotalSendRequest = 0;

            await _api.PatchGroupRecalculate(group);

            var task_ch = await _api.GetTaskChatByIdChat((long)channelOrChat.IdGroup!);
            if (task_ch is null)
            {
                task_ch = new TaskChatDTO()
                {
                    IdChat = (long)channelOrChat.IdGroup!,
                    OffsetIdNewMessage = -1,
                    OffsetIdOldMessage = -1
                };
                await _api.PostTaskChat(task_ch);
            }
            else
            {
                task_ch.OffsetIdNewMessage = -1;
                task_ch.OffsetIdOldMessage = -1;
                await _api.PutTaskChat((long)task_ch.Id!, task_ch);
            }

            WordGroupAllForWrite wordGroupAllForWrite = new()
            {
                TopicName = _topicsKafka.GetTopic(TopicResolve.Word_Group_All),
                ModesForDB = new() { ModesForDB.UPDATE },
                ChatId = channelOrChat.IdGroup.ToString()!,
                WordGroupAlls = new()
            {
                channelOrChat
            }
            };
            await _kafkaSendMessage(wordGroupAllForWrite);
        }

        private async Task GroupInitialAsync(WordGroupAllDTO group, WordGroupAllDTO channelOrChat, string? hashGroup = default)
        {
            while (!_cancellationToken.IsCancellationRequested)
            {
                try
                {
                    var group_from_db = await _api.GetGroupByIdGroup((long)channelOrChat.IdGroup!);
                    if (group_from_db is not null)
                    {
                        if (group.Id != group_from_db.Id)
                        {
                            if (group_from_db.HandlersId == -1)
                            {
                                await UpdateOldOffGroup(group_from_db, channelOrChat);
                                await RemoveGroupAsync(group);
                            }
                            else
                            {
                                await RemoveGroupAsync(group);
                            }
                        }
                    }
                    else
                    {
                        group.Type = channelOrChat.Type;
                        group.IdGroup = channelOrChat.IdGroup;
                        group.FindGroup = channelOrChat.FindGroup;
                        group.HashGroup = channelOrChat.HashGroup;
                        group.TitleGroup = channelOrChat.TitleGroup;
                        group.Flags = channelOrChat.Flags;
                        group.Flags2 = channelOrChat.Flags2;
                        group.ParticipantsCount = channelOrChat.ParticipantsCount;
                        group.CreatedDate = channelOrChat.CreatedDate;
                        group.InfoGroup = channelOrChat.InfoGroup;
                        group.LinkedId = channelOrChat.LinkedId;

                        await _api.PutGroup((long)group.Id!, group);

                        var task_ch = await _api.GetTaskChatByIdChat((long)channelOrChat.IdGroup!);
                        if (task_ch is null)
                        {
                            task_ch = new TaskChatDTO()
                            {
                                IdChat = (long)channelOrChat.IdGroup!,
                                OffsetIdNewMessage = -1,
                                OffsetIdOldMessage = -1
                            };

                            await _api.PostTaskChat(task_ch);
                        }
                        else
                        {
                            task_ch.OffsetIdNewMessage = -1;
                            task_ch.OffsetIdOldMessage = -1;
                            await _api.PutTaskChat((long)task_ch.Id!, task_ch);
                        }
                    }
                    break;
                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = $"Func [GroupInitialAsync] [message: {ex.Message}] [innerException: {ex.InnerException?.Message}]",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                }
                await Task.Delay(TimeSpan.FromSeconds(5), cancellationToken: _cancellationToken);
            }
        }

        private async Task<Dictionary<long, ChatBase>?> GetFullChat(TL.InputChannel inputChannel, string? hashGroup)
        {
            Dictionary<long, ChatBase>? chats = null;
            TL.Messages_ChatFull? channelOrChatFull = null;
            while (!_cancellationToken.IsCancellationRequested)
            {
                try
                {
                    _logger.Log(new Log()
                    {
                        Message = $"Выполняем GetFullChat...",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Info
                    });
                    channelOrChatFull = await _clientBase.GetFullChat(inputChannel);
                    await Task.Delay(TimeSpan.FromSeconds((int)_parseOptions.DelayGetFullChatS!), _cancellationToken);
                    break;
                }
                catch (RpcException rpcException)
                {
                    _logger.Log(new Log()
                    {
                        Message = $"RPCException [GetFullChat:GetFullChat] [code {rpcException.Code}] [message: {rpcException.Message}]",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });

                    if (rpcException.Code == 400 || rpcException.Code == 406)
                    {
                        var stat = await JoinChannel(inputChannel);
                        if (stat == "INVITE_REQUEST_SENT")
                        {
                            await RequestSentFuncAsync(inputChannel.channel_id);
                        }
                        else if (stat != "JOINED")
                        {
                            await DetectChannelPrivate(inputChannel.channel_id);
                        }
                        break;
                    }
                    if (rpcException.Message == "FLOOD_WAIT_X")
                    {
                        _logger.Log(new Log()
                        {
                            Message = $"FLOOD_WAIT_X [X = {rpcException.X}]...",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Warning
                        });
                        break;
                    }
                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = $"Exception [message: {ex.Message}]",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                    break;
                }
            }

            if (channelOrChatFull is not null)
            {
                chats = channelOrChatFull.chats;
                long? participantsCount = channelOrChatFull.full_chat.ParticipantsCount > 0 ? channelOrChatFull.full_chat.ParticipantsCount : null;
                var exportedInvite = channelOrChatFull.full_chat.ExportedInvite;
                if (exportedInvite is not null)
                {
                    //hz
                }
                bool flagAddGroup = false;

                var chat_2 = channelOrChatFull.chats.Select(x => x.Value).FirstOrDefault(x => x.ID != inputChannel.channel_id);
                var chat_1 = channelOrChatFull.chats.FirstOrDefault(x => x.Key == inputChannel.channel_id).Value;

                if (chat_1 is not null)
                {
                    WordGroupAllDTO? chatOrChannelDto = null;
                    if (chat_1 is TL.Channel chan)
                    {
                        chatOrChannelDto = ChannelToWGA(chan, hashGroup, channelOrChatFull.full_chat.About, chat_2?.ID, participantsCount);
                    }
                    else if (chat_1 is TL.Chat chat)
                    {
                        chatOrChannelDto = ChatToWGA(chat, hashGroup, channelOrChatFull.full_chat.About, chat_2?.ID, participantsCount);
                    }
                    if (chatOrChannelDto != null)
                    {
                        WordGroupAllForWrite wordGroupAllForWrite = new()
                        {
                            TopicName = _topicsKafka.GetTopic(TopicResolve.Word_Group_All),
                            ChatId = chatOrChannelDto.IdGroup.ToString()!,
                            ModesForDB = new() { ModesForDB.UPDATE },
                            WordGroupAlls = new()
                    {
                        chatOrChannelDto
                    }
                        };
                        await _kafkaSendMessage(wordGroupAllForWrite);
                    }
                }

                if (chat_2 is not null)
                {
                    WordGroupAllDTO? chatOrChannelDto = null;
                    long? accessHash = null;
                    if (chat_2 is TL.Channel chan)
                    {
                        chatOrChannelDto = ChannelToWGA(chan, hashGroup: hashGroup, linked_id: channelOrChatFull.full_chat.ID, participants_count: participantsCount);
                        accessHash = chan.access_hash;
                    }
                    else if (chat_2 is TL.Chat chat)
                    {
                        chatOrChannelDto = ChatToWGA(chat, hashGroup: hashGroup, linked_id: channelOrChatFull.full_chat.ID, participants_count: participantsCount);
                        if (chat.migrated_to is TL.InputChannel inputCh2)
                        {
                            accessHash = inputCh2.access_hash;
                        }
                    }
                    if (chatOrChannelDto != null)
                    {
                        var group_from_db = await _api.GetGroupByIdGroup(chat_2.ID);
                        if (group_from_db is not null)
                        {
                            if (group_from_db.HandlersId == -1)
                            {
                                await UpdateOldOffGroup(group_from_db, chatOrChannelDto);
                            }
                            else
                            {
                                flagAddGroup = true;
                            }
                        }
                        else
                        {
                            chatOrChannelDto.HandlersId = _handlerId;
                            var taskChNew = await _api.GetTaskChatByIdChat(chat_2.ID);
                            if (taskChNew is null)
                            {
                                taskChNew = new TaskChatDTO()
                                {
                                    IdChat = chat_2.ID,
                                    OffsetIdNewMessage = -1,
                                    OffsetIdOldMessage = -1
                                };

                                await _api.PostTaskChat(taskChNew);
                            }
                            else
                            {
                                taskChNew.OffsetIdNewMessage = -1;
                                taskChNew.OffsetIdOldMessage = -1;
                                await _api.PutTaskChat((long)taskChNew.Id!, taskChNew);
                            }
                            await _api.PostGroup(chatOrChannelDto);
                        }
                    }

                    if (accessHash is not null)
                    {
                        _parseTelegramCore.AddOrUpdateChannels(chat_2.ID, (long)accessHash);
                    }

                    if (flagAddGroup)
                    {
                        chats.Remove(chat_2.ID);
                    }
                }
            }
            return chats;
        }

        private async Task<(ChatBase? searchChat, ChatBase? linkedChat)> InitialUnknowChat(WordGroupAllDTO group)
        {
            TL.ChatBase? resultChat = null;
            TL.ChatBase? chatForComments = null;

            if (!string.IsNullOrEmpty(group.FindGroup))
            {
                Contacts_ResolvedPeer? resolvedPeer = null;
                while (!_cancellationToken.IsCancellationRequested)
                {
                    try
                    {
                        _logger.Log(new Log()
                        {
                            Message = $"Выполняем ResolveUsername [find_group = {group.FindGroup}]...",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Info
                        });
                        resolvedPeer = await _clientBase.Contacts_ResolveUsername(group.FindGroup);
                        await Task.Delay(TimeSpan.FromSeconds((int)_parseOptions.DelayResolveUsernameS!), _cancellationToken);
                        break;
                    }
                    catch (RpcException rpcException)
                    {
                        _logger.Log(new Log()
                        {
                            Message = $"RPCException [InitialUnknowChat:ResolveUsername] [code {rpcException.Code}] [message: {rpcException.Message}]",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Error
                        });

                        if (rpcException.Code == 400 || rpcException.Code == 406)
                        {
                            await RemoveGroupAsync(group);
                            break;
                        }
                        if (rpcException.Message == "FLOOD_WAIT_X")
                        {
                            _logger.Log(new Log()
                            {
                                Message = $"FLOOD_WAIT_X [X = {rpcException.X}]...",
                                LogDT = DateTime.UtcNow,
                                Level = Levels.Warning
                            });
                            break;
                        }
                    }
                    catch (Exception ex)
                    {
                        _logger.Log(new Log()
                        {
                            Message = $"Exception [message: {ex.Message}]",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Error
                        });
                        break;
                    }
                }
                if (resolvedPeer is not null)
                {
                    if (resolvedPeer.Channel != null)
                    {
                        _parseTelegramCore.AddOrUpdateChannels(resolvedPeer.Channel.ID, resolvedPeer.Channel.access_hash);
                        await GroupInitialAsync(group, ChannelToWGA(resolvedPeer.Channel));
                        resultChat = resolvedPeer.Channel;
                    }
                    else if (resolvedPeer.Chat is TL.Chat tlChat)
                    {
                        if (tlChat.migrated_to is TL.InputChannel inputChannel)
                        {
                            _parseTelegramCore.AddOrUpdateChannels(inputChannel.channel_id, inputChannel.access_hash);
                        }

                        await GroupInitialAsync(group, ChatToWGA(tlChat));
                        resultChat = resolvedPeer.Chat;
                    }
                    else
                    {
                        _logger.Log(new Log()
                        {
                            Message = $"Func [InitialUnknowChat] не является chat и channel [{group.FindGroup}]",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Info
                        });
                        await RemoveGroupAsync(group);
                    }
                }
            }
            else if (!string.IsNullOrEmpty(group.HashGroup))
            {
                TL.ChatInviteBase? chatInviteBase = null;
                while (!_cancellationToken.IsCancellationRequested)
                {
                    try
                    {
                        chatInviteBase = await _clientBase.Messages_CheckChatInvite(group.HashGroup);
                        await Task.Delay(TimeSpan.FromSeconds(50), _cancellationToken);
                        break;
                    }
                    catch (RpcException rpcException)
                    {
                        _logger.Log(new Log()
                        {
                            Message = $"RPCException [InitialUnknowChat:CheckChatInvite] [code {rpcException.Code}] [message: {rpcException.Message}]",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Error
                        });

                        if (rpcException.Code == 400 || rpcException.Code == 406)
                        {
                            await RemoveGroupAsync(group);
                            break;
                        }
                    }
                    catch (Exception ex)
                    {
                        _logger.Log(new Log()
                        {
                            Message = $"Exception [message: {ex.Message}]",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Error
                        });
                        break;
                    }
                }
                if (chatInviteBase is not null)
                {
                    if (chatInviteBase is TL.ChatInviteAlready chatInviteAlready)
                    {
                        if (chatInviteAlready.chat is TL.Channel channel)
                        {
                            _parseTelegramCore.AddOrUpdateChannels(channel.ID, channel.access_hash);
                            await GroupInitialAsync(group, ChannelToWGA(channel, hashGroup: group.HashGroup));
                            resultChat = channel;
                        }
                        else if (chatInviteAlready.chat is TL.Chat tlChat)
                        {
                            if (tlChat.migrated_to is TL.InputChannel inputChannel)
                            {
                                _parseTelegramCore.AddOrUpdateChannels(inputChannel.channel_id, inputChannel.access_hash);
                            }

                            await GroupInitialAsync(group, ChatToWGA(tlChat, hashGroup: group.HashGroup));
                            resultChat = tlChat;
                        }
                        else
                        {
                            _logger.Log(new Log()
                            {
                                Message = $"Func [InitialUnknowChat:chatInviteAlready] не является chat и channel [{group.HashGroup}]",
                                LogDT = DateTime.UtcNow,
                                Level = Levels.Info
                            });
                            await RemoveGroupAsync(group);
                        }
                    }
                    else if (chatInviteBase is TL.ChatInvitePeek chatInvitePeek)
                    {
                        if (chatInvitePeek.chat is TL.Channel channel)
                        {
                            _parseTelegramCore.AddOrUpdateChannels(channel.ID, channel.access_hash);
                            await GroupInitialAsync(group, ChannelToWGA(channel, hashGroup: group.HashGroup));
                            resultChat = channel;
                        }
                        else if (chatInvitePeek.chat is TL.Chat tlChat)
                        {
                            if (tlChat.migrated_to is TL.InputChannel inputChannel)
                            {
                                _parseTelegramCore.AddOrUpdateChannels(inputChannel.channel_id, inputChannel.access_hash);
                            }

                            await GroupInitialAsync(group, ChatToWGA(tlChat, hashGroup: group.HashGroup));
                            resultChat = tlChat;
                        }
                        else
                        {
                            _logger.Log(new Log()
                            {
                                Message = $"Func [InitialUnknowChat:chatInvitePeek] не является chat и channel [{group.HashGroup}]",
                                LogDT = DateTime.UtcNow,
                                Level = Levels.Info
                            });
                            await RemoveGroupAsync(group);
                        }
                        if (chatInvitePeek.chat is not null)
                        {
                            while (!_cancellationToken.IsCancellationRequested)
                            {
                                try
                                {
                                    var joinChannel = await _clientBase.Messages_ImportChatInvite(group.HashGroup);
                                    if (joinChannel.Chats.Count > 0)
                                    {
                                        ChatBase chatBase = joinChannel.Chats.FirstOrDefault().Value;
                                        if (chatBase is TL.Channel chan)
                                        {
                                            _parseTelegramCore.AddOrUpdateChannels(chan.ID, chan.access_hash);
                                            await GroupInitialAsync(group, ChannelToWGA(chan, hashGroup: group.HashGroup));
                                            resultChat = chan;
                                        }
                                        else if (chatBase is TL.Chat tlChat)
                                        {
                                            if (tlChat.migrated_to is TL.InputChannel inputChannel)
                                            {
                                                _parseTelegramCore.AddOrUpdateChannels(inputChannel.channel_id, inputChannel.access_hash);
                                            }

                                            await GroupInitialAsync(group, ChatToWGA(tlChat, hashGroup: group.HashGroup));
                                            resultChat = tlChat;
                                        }
                                        else
                                        {
                                            _logger.Log(new Log()
                                            {
                                                Message = $"Func [InitialUnknowChat:chatInvitePeek] не является chat и channel [{group.HashGroup}]",
                                                LogDT = DateTime.UtcNow,
                                                Level = Levels.Info
                                            });
                                            await RemoveGroupAsync(group);
                                        }
                                        _logger.Log(new Log()
                                        {
                                            Message = $"Вступил в чат/канал по ссылке-приглашению [channel: {chatBase.MainUsername} | {chatBase.Title} | {chatBase.ID}]",
                                            LogDT = DateTime.UtcNow,
                                            Level = Levels.Info
                                        });
                                    }
                                    await Task.Delay(TimeSpan.FromSeconds(50), _cancellationToken);
                                    break;
                                }
                                catch (RpcException rpcException)
                                {
                                    _logger.Log(new Log()
                                    {
                                        Message = $"RPCException [InitialUnknowChat:ImportChatInvite] [code {rpcException.Code}] [message: {rpcException.Message}]",
                                        LogDT = DateTime.UtcNow,
                                        Level = Levels.Error
                                    });

                                    if ((rpcException.Code == 400 || rpcException.Code == 406) && rpcException.Message != "INVITE_REQUEST_SENT" &&
                                        rpcException.Message != "USER_ALREADY_PARTICIPANT")
                                    {
                                        await RemoveGroupAsync(group);
                                        break;
                                    }
                                    if (rpcException.Message == "FLOOD_WAIT_X")
                                    {
                                        _logger.Log(new Log()
                                        {
                                            Message = $"FLOOD_WAIT_X [X = {rpcException.X}]...",
                                            LogDT = DateTime.UtcNow,
                                            Level = Levels.Warning
                                        });
                                        break;
                                    }
                                    if (rpcException.Message == "INVITE_REQUEST_SENT")
                                    {
                                        await RequestSentFuncAsync(chatInvitePeek.chat.ID);
                                        break;
                                    }
                                }
                                catch (Exception ex)
                                {
                                    _logger.Log(new Log()
                                    {
                                        Message = $"Exception [message: {ex.Message}]",
                                        LogDT = DateTime.UtcNow,
                                        Level = Levels.Error
                                    });
                                    break;
                                }
                            }
                        }
                    }
                    else
                    {
                        while (!_cancellationToken.IsCancellationRequested)
                        {
                            try
                            {
                                var joinChannel = await _clientBase.Messages_ImportChatInvite(group.HashGroup);
                                if (joinChannel.Chats.Count > 0)
                                {
                                    ChatBase chatBase = joinChannel.Chats.FirstOrDefault().Value;
                                    if (chatBase is TL.Channel chan)
                                    {
                                        _parseTelegramCore.AddOrUpdateChannels(chan.ID, chan.access_hash);
                                        await GroupInitialAsync(group, ChannelToWGA(chan, hashGroup: group.HashGroup));
                                        resultChat = chan;
                                    }
                                    else if (chatBase is TL.Chat tlChat)
                                    {
                                        if (tlChat.migrated_to is TL.InputChannel inputChannel)
                                        {
                                            _parseTelegramCore.AddOrUpdateChannels(inputChannel.channel_id, inputChannel.access_hash);
                                        }

                                        await GroupInitialAsync(group, ChatToWGA(tlChat, hashGroup: group.HashGroup));
                                        resultChat = tlChat;
                                    }
                                    else
                                    {
                                        _logger.Log(new Log()
                                        {
                                            Message = $"Func [InitialUnknowChat:chatInvitePeek] не является chat и channel [{group.HashGroup}]",
                                            LogDT = DateTime.UtcNow,
                                            Level = Levels.Info
                                        });
                                        await RemoveGroupAsync(group);
                                    }
                                    _logger.Log(new Log()
                                    {
                                        Message = $"Вступил в чат/канал по ссылке-приглашению [channel: {chatBase.MainUsername} | {chatBase.Title} | {chatBase.ID}]",
                                        LogDT = DateTime.UtcNow,
                                        Level = Levels.Info
                                    });
                                }
                                await Task.Delay(TimeSpan.FromSeconds(50), _cancellationToken);
                                break;
                            }
                            catch (RpcException rpcException)
                            {
                                _logger.Log(new Log()
                                {
                                    Message = $"RPCException [InitialUnknowChat:ImportChatInvite2] [code {rpcException.Code}] [message: {rpcException.Message}]",
                                    LogDT = DateTime.UtcNow,
                                    Level = Levels.Error
                                });

                                if ((rpcException.Code == 400 || rpcException.Code == 406) && rpcException.Message != "INVITE_REQUEST_SENT" &&
                                    rpcException.Message != "USER_ALREADY_PARTICIPANT")
                                {
                                    await RemoveGroupAsync(group);
                                    break;
                                }
                                if (rpcException.Message == "FLOOD_WAIT_X")
                                {
                                    _logger.Log(new Log()
                                    {
                                        Message = $"FLOOD_WAIT_X [X = {rpcException.X}]...",
                                        LogDT = DateTime.UtcNow,
                                        Level = Levels.Warning
                                    });
                                    break;
                                }
                                if (rpcException.Message == "INVITE_REQUEST_SENT")
                                {
                                    long? totalSend = await GetSendRequestAsync(hashGroup: group.HashGroup);
                                    if (totalSend is not null)
                                    {
                                        _logger.Log(new Log()
                                        {
                                            Message = $"Запросы на вступление к {group.HashGroup} превышены",
                                            LogDT = DateTime.UtcNow,
                                            Level = Levels.Warning
                                        });
                                        await RemoveGroupAsync(group);
                                    }
                                    else
                                    {
                                        await SetSendRequestAsync(hashGroup: group.HashGroup);
                                        _logger.Log(new Log()
                                        {
                                            Message = $"Запрос на вступление к [ hash {group.HashGroup}] отправлен",
                                            LogDT = DateTime.UtcNow,
                                            Level = Levels.Info
                                        });
                                    }

                                    break;
                                }
                            }
                            catch (Exception ex)
                            {
                                _logger.Log(new Log()
                                {
                                    Message = $"Exception [message: {ex.Message}]",
                                    LogDT = DateTime.UtcNow,
                                    Level = Levels.Error
                                });
                                break;
                            }
                        }
                    }
                }
            }
            else
            {
                _logger.Log(new Log()
                {
                    Message = $"FindGroup и Hash являются null [id: {group.IdGroup}]",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Error
                });
                await RemoveGroupAsync(group);
            }

            if (resultChat is not null)
            {
                TL.InputChannel? inputChannel = null;
                if (resultChat is TL.Channel channel)
                {
                    inputChannel = new InputChannel(channel.id, channel.access_hash);
                }
                else if (resultChat is TL.Chat chat)
                {
                    if (chat.migrated_to is TL.InputChannel inpCh)
                    {
                        inputChannel = inpCh;
                    }
                }
                if (inputChannel is not null)
                {
                    var chats = await GetFullChat(inputChannel, group.HashGroup);
                    chatForComments = chats?.FirstOrDefault(x => x.Key != inputChannel.channel_id).Value;
                }
            }
            return (resultChat, chatForComments);
        }

        public async Task LoaderGroup()
        {
            while (!_cancellationToken.IsCancellationRequested)
            {
                _logger.Log(new Log()
                {
                    Message = $"Начата подгрузка групп...",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Info
                });
                try
                {
                    long offset_id = 0;
                    int batchSize = 100;
                    int countGroupsLoad = 0;
                    while (!_cancellationToken.IsCancellationRequested)
                    {
                        List<WordGroupAllDTO>? groups = await _api.GetGroupBatch(_handlerId, offset_id, limit: batchSize);
                        if (groups == null || groups?.Count == 0)
                        {
                            break;
                        }

                        _logger.Log(new Log()
                        {
                            Message = $"Подгружено groups [count = {groups!.Count}]",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Info
                        });
                        countGroupsLoad += groups!.Count;
                        offset_id = (long)groups[^1].Id!;
                        groups = groups.OrderBy(x => x.LastHandle).ToList();

                        foreach (var group in groups)
                        {
                            if (_cancellationToken.IsCancellationRequested) return;
                            KeyValuePair<long, long> entity = default;
                            if (group.IdGroup is not null)
                            {
                                if (_parseTelegramCore.IsContainsQueue((long)group.IdGroup))
                                {
                                    continue;
                                }
                                entity = _parseTelegramCore.GetByIdGroupSavedState((long)group.IdGroup);
                            }
                            if (entity.Key != 0)
                            {
                                var result = await GetFullChat(new InputChannel(entity.Key, entity.Value), group.HashGroup);
                                if (result is not null)
                                {
                                    foreach (var item in result)
                                    {
                                        _parseTelegramCore.AddChatQueue(item.Value);
                                        _logger.Log(new Log()
                                        {
                                            Message = $"Добавлена группа/канал {item.Value.ID}:{item.Value.Title} по ACCESS_HASH",
                                            LogDT = DateTime.UtcNow,
                                            Level = Levels.Info
                                        });
                                    }
                                }
                            }
                            else
                            {
                                var result = await InitialUnknowChat(group);
                                if (result.searchChat is not null)
                                {
                                    _parseTelegramCore.AddChatQueue(result.searchChat);
                                    _logger.Log(new Log()
                                    {
                                        Message = $"Добавлена группа/канал {result.searchChat.ID}:{result.searchChat.Title} по INITIAL_UNKNOW",
                                        LogDT = DateTime.UtcNow,
                                        Level = Levels.Info
                                    });
                                }
                                if (result.linkedChat is not null)
                                {
                                    _parseTelegramCore.AddChatQueue(result.linkedChat);
                                    _logger.Log(new Log()
                                    {
                                        Message = $"""
                                Добавлена группа/канал {result.linkedChat.ID}:{result.linkedChat.Title}, 
                                зависимая от {result.searchChat?.ID}:{result.searchChat?.Title}
                                """,
                                        LogDT = DateTime.UtcNow,
                                        Level = Levels.Info
                                    });
                                }
                            }
                            await _parseTelegramCore.CollectWriteHashesAsync();
                        }
                    }
                    _logger.Log(new Log()
                    {
                        Message = $"Подргрузка групп окончена! [load = {countGroupsLoad}]",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Info
                    });
                    countGroupsLoad = 0;
                    await Task.Delay(TimeSpan.FromSeconds((int)_parseOptions.DelayLoaderGroupS!), _cancellationToken);
                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = "Func [LoaderGroup]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                }

            }
        }

        public async Task DetectChannelPrivate(long idGroup, bool flagDisabled = false)
        {
            try
            {
                _parseTelegramCore.RemoveFromSavedState(idGroup);
                _parseTelegramCore.TryAddDetectChannelPrivate(idGroup);
                if (!flagDisabled)
                {
                    var group = await _api.GetGroupByIdGroup(idGroup);
                    if (group is not null)
                    {
                        if (group.TotalDetectPrivate > _parseOptions.TotalDetectPrivate)
                        {
                            group.HandlersId = -1;
                            group.TotalDetectPrivate = 0;
                            group.TotalSendRequest = 0;

                            await _api.PatchGroupRecalculate(group);
                        }
                        else
                        {
                            var IDs = await _api.GetHandlersIdsByCategory("ParseGroup");
                            if (IDs is null)
                                return;
                            var rand_hand = _random.Next(0, IDs.Count);
                            group.HandlersId = IDs[rand_hand];
                            group.TotalDetectPrivate += 1;
                            await _api.PatchGroupRecalculate(group);
                        }
                    }
                }
                else
                {
                    var group = await _api.GetGroupByIdGroupAndHandlersId(idGroup, _handlerId);
                    if (group is not null)
                    {
                        group.HandlersId = -1;
                        await _api.PatchGroupRecalculate(group);
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.Log(new Log()
                {
                    Message = $"Func [DetectChannelPrivate] [message: {ex.Message}] [innerException: {ex.InnerException?.Message}]",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Error
                });
            }
        }

        public async Task RequestSentFuncAsync(long channelId)
        {
            long? totalSend = await GetSendRequestAsync(idGroup: channelId);
            if (totalSend is not null)
            {
                _logger.Log(new Log()
                {
                    Message = $"Запросы на вступление к {channelId} превышены",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Info
                });
                await DetectChannelPrivate(channelId, flagDisabled: true);
            }
            else
            {
                await SetSendRequestAsync(idGroup: channelId);
                _logger.Log(new Log()
                {
                    Message = $"Запрос на вступление к {channelId} отправлен",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Info
                });
            }
        }

        public async Task<string?> JoinChannel(TL.InputChannel inputChannel)
        {
            string? result = null;
            while (!_cancellationToken.IsCancellationRequested)
            {
                try
                {
                    var resultJoin = await _clientBase.Channels_JoinChannel(inputChannel);
                    await Task.Delay(TimeSpan.FromSeconds((int)_parseOptions.DelayJoinChannelS!), _cancellationToken);
                    return "JOINED";
                }
                catch (RpcException rpcException)
                {
                    _logger.Log(new Log()
                    {
                        Message = $"RPCException [JoinChannel:JoinChannel] [code {rpcException.Code}] [message: {rpcException.Message}]",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });

                    if (rpcException.Code == 400 || rpcException.Code == 406)
                    {
                        return rpcException.Message;
                    }
                    if (rpcException.Message == "FLOOD_WAIT_X")
                    {
                        _logger.Log(new Log()
                        {
                            Message = $"FLOOD_WAIT_X [X = {rpcException.X}]...",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Warning
                        });
                        break;
                    }
                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = $"Func [JoinChannel] [message: {ex.Message}]",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                    break;
                }
            }
            return result;
        }

        public async Task UpdateGroup()
        {
            while (!_cancellationToken.IsCancellationRequested)
            {
                try
                {
                    await Task.Delay(TimeSpan.FromHours((int)_parseOptions.DelayUpdateGroupH!), _cancellationToken);
                    _logger.Log(new Log()
                    {
                        Message = $"Началось обновление групп...",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Info
                    });
                    long? offset_id = 0;
                    int batchSize = 100;
                    while (!_cancellationToken.IsCancellationRequested)
                    {
                        List<WordGroupAllDTO>? groups = await _api.GetGroupBatch(_handlerId, (long)offset_id!, limit: batchSize);

                        if (groups == null || groups?.Count == 0)
                        {
                            break;
                        }
                        offset_id = groups![^1].Id;
                        _logger.Log(new Log()
                        {
                            Message = $"Подгружено groups для UPDATE [count = {groups!.Count}]",
                            LogDT = DateTime.UtcNow,
                            Level = Levels.Info
                        });

                        foreach (var group in groups)
                        {
                            if (_cancellationToken.IsCancellationRequested) return;
                            KeyValuePair<long, long> entity = default;
                            if (group.IdGroup is not null)
                            {
                                entity = _parseTelegramCore.GetByIdGroupSavedState((long)group.IdGroup);
                            }
                            if (entity.Key != 0)
                            {
                                var result = await GetFullChat(new InputChannel(entity.Key, entity.Value), group.HashGroup);
                                if (result is not null)
                                {
                                    foreach (var item in result)
                                    {
                                        _logger.Log(new Log()
                                        {
                                            Message = $"Обновлена группа/канал {item.Value.ID}:{item.Value.Title}",
                                            LogDT = DateTime.UtcNow,
                                            Level = Levels.Info
                                        });
                                    }
                                }
                            }
                        }
                    }

                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = "Func [UpdateGroup]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                }
            }
        }
    }
}