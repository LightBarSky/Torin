using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection.Metadata.Ecma335;
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
    interface IWriters
    {
        Task WriteUserAndChatAsync(List<UserAndFullUser> paticipants, long idChat, Dictionary<long, ChannelParticipant>? participants = default);

        Task WriteUserAdminOrCreatorAsync(List<AdminChatDTO> adminChats, long chatId);

        Task UpdateTaskChatAsync(List<TaskChatDTO> taskChats, long chatId);

        Task WriteMessages(List<MessageDTO> messages, long chatId);

        Task WriteMessagesProperties(List<MessagesPropertiesDTO> messagesProp, long chatId);

        Task WriteMessageEntities(List<MessagesEntitiesDTO> messagesEntities, long chatId);

        Task WriteReactions(List<ReactionDTO> reactions, long chatId);

        Task WriteReactionsGeneral(List<ReactionGeneralDTO> reactionsGeneral, long chatId);

        Task WriteGifts(List<GiftsDTO> gifts, long chatId);

        Task WriterParseMessage(ParseMessageStorage storage, long chat_id);
    }
    internal class Writers: IWriters
    {
        private ParseTelegramCore _parseTelegramCore;
        private ILogger _logger => _parseTelegramCore.Logger;
        private ParseGroupOptions _parseOptions => _parseTelegramCore.ParseOptions;
        private Func<IKafkaMessageCreator, Task> _kafkaSendMessage => _parseTelegramCore.KafkaSendMessage;
        private TopicsKafka _topicsKafka => _parseTelegramCore.TopicsKafka;
        private CancellationToken _cancellationToken => _parseTelegramCore.CancellationToken;

        public Writers(ParseTelegramCore parseTelegramCore)
        {
            _parseTelegramCore = parseTelegramCore;
        }

        public async Task WriteUserAndChatAsync(List<UserAndFullUser> paticipants, long idChat, Dictionary<long, ChannelParticipant>? participants = default)
        {
            bool flagExit = false;
            while (!flagExit && !_cancellationToken.IsCancellationRequested)
            {
                try
                {
                    int batchSize = (int)_parseOptions.BatchForKafka!;
                    int offset = 0;

                    while (!_cancellationToken.IsCancellationRequested)
                    {
                        var part = paticipants.Skip(offset).Take(batchSize).ToList();
                        if (part.Count == 0)
                        {
                            flagExit = true;
                            break;
                        }

                        UsersForWriteOrUpdate usersForWrite = new() { ChatId = idChat.ToString(), TopicName = _topicsKafka.GetTopic(TopicResolve.User) };
                        ChatForWrite chatForWrite = new() { ChatId = idChat.ToString(), TopicName = _topicsKafka.GetTopic(TopicResolve.Chat) };

                        foreach (var participant in part)
                        {
                            if (participant.User is null) continue;
                            if (participant.User.flags.HasFlag(TL.User.Flags.deleted)) continue;

                            var user = new UserDTO()
                            {
                                IdUser = participant.User.ID,
                                FirstName = participant.User.first_name,
                                LastName = participant.User.last_name,
                                Username = participant.User.username,
                                Number = participant.User.phone,
                                UpdatedAt = DateTime.UtcNow,
                                Flags = participant.User.flags.ToString(),
                                Flags2 = participant.User.flags2.ToString(),
                                IsBot = participant.User.IsBot
                            };
                            if (participant.FullUser is not null)
                            {
                                user.About = participant.FullUser.about;
                                if (participant.FullUser.birthday is not null)
                                {
                                    string birthday = string.Empty;
                                    if (participant.FullUser.birthday.flags.HasFlag(Birthday.Flags.has_year))
                                    {
                                        birthday = $"{participant.FullUser.birthday.day}.{participant.FullUser.birthday.month}.{participant.FullUser.birthday.year}";
                                    }
                                    else
                                    {
                                        birthday = $"{participant.FullUser.birthday.day}.{participant.FullUser.birthday.month}";
                                    }
                                    user.Birthday = birthday;
                                }
                                user.FlagsFull = participant.FullUser.flags.ToString();
                                user.Flags2Full = participant.FullUser.flags2.ToString();
                                user.BotInfo = participant.FullUser.bot_info?.description;
                                user.PersonalChannelId = participant.FullUser.personal_channel_id;
                                user.LocationAddress = participant.FullUser.business_location?.address;
                                user.LocationLat = participant.FullUser.business_location?.geo_point?.lat;
                                user.LocationLon = participant.FullUser.business_location?.geo_point?.lon;
                                user.LocationRadius = participant.FullUser.business_location?.geo_point?.accuracy_radius;
                                if (!string.IsNullOrEmpty(user.LocationAddress) || user.LocationLat != null)
                                {
                                    user.IsGeo = true;
                                }
                            }
                            usersForWrite.Users.Add(user);
                            DateTime? dateJoined = null;
                            if (participants is not null && participants.ContainsKey(user.IdUser))
                            {
                                dateJoined = participants[user.IdUser].date;
                            }
                            chatForWrite.Chats.Add(new ChatDTO()
                            {
                                IdGroup = idChat,
                                IdUser = participant.User.ID,
                                DateJoined = dateJoined
                            });
                        }
                        chatForWrite.ModesForDB.AddRange([ModesForDB.INSERT_IGNORE]);
                        usersForWrite.ModesForDB.AddRange([ModesForDB.INSERT, ModesForDB.UPDATE]);
                        if (usersForWrite.Users.Count > 0)
                        {
                            await _kafkaSendMessage(usersForWrite);
                        }
                        if (chatForWrite.Chats.Count > 0)
                        {
                            await _kafkaSendMessage(chatForWrite);
                        }
                        offset += part.Count;
                    }
                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = "Func [WriteUser]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                    await Task.Delay(60_000, cancellationToken: _cancellationToken);
                }
            }
        }

        public async Task WriteUserAdminOrCreatorAsync(List<AdminChatDTO> adminChats, long chatId)
        {
            bool flagExit = false;
            while (!flagExit && !_cancellationToken.IsCancellationRequested)
            {
                try
                {
                    int batchSize = (int)_parseOptions.BatchForKafka!;
                    int offset = 0;

                    while (!_cancellationToken.IsCancellationRequested)
                    {
                        var part = adminChats.Skip(offset).Take(batchSize).ToList();
                        if (part.Count == 0)
                        {
                            flagExit = true;
                            break;
                        }
                        AdminChatsForWrite adminChatsForWrite = new()
                        {
                            ChatId = chatId.ToString(),
                            TopicName = _topicsKafka.GetTopic(TopicResolve.Admin_Chats),
                            AdminChats = part
                        };
                        adminChatsForWrite.ModesForDB.AddRange([ModesForDB.INSERT_IGNORE]);
                        if (adminChatsForWrite.AdminChats.Count > 0)
                        {
                            await _kafkaSendMessage(adminChatsForWrite);
                        }
                        offset += part.Count;
                    }
                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = "Func [WriteUser]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                    await Task.Delay(60_000, cancellationToken: _cancellationToken);
                }
            }
        }

        public async Task UpdateTaskChatAsync(List<TaskChatDTO> taskChats, long chatId)
        {
            bool flagExit = false;
            while (!flagExit && !_cancellationToken.IsCancellationRequested)
            {
                try
                {
                    int batchSize = (int)_parseOptions.BatchForKafka!;
                    int offset = 0;

                    while (!_cancellationToken.IsCancellationRequested)
                    {
                        var part = taskChats.Skip(offset).Take(batchSize).ToList();
                        if (part.Count == 0)
                        {
                            flagExit = true;
                            break;
                        }
                        TaskChatsForWrite taskChatsForWrite = new()
                        {
                            ChatId = chatId.ToString(),
                            TopicName = _topicsKafka.GetTopic(TopicResolve.Task_Chats),
                            TaskChats = part
                        };
                        taskChatsForWrite.ModesForDB.AddRange([ModesForDB.UPDATE]);
                        if (taskChatsForWrite.TaskChats.Count > 0)
                        {
                            await _kafkaSendMessage(taskChatsForWrite);
                        }
                        offset += part.Count;
                    }
                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = "Func [UpdateTaskChat]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                    await Task.Delay(60_000, cancellationToken: _cancellationToken);
                }
            }
        }

        public async Task WriteMessages(List<MessageDTO> messages, long chatId)
        {
            bool flagExit = false;
            while (!flagExit && !_cancellationToken.IsCancellationRequested)
            {
                try
                {
                    int batchSize = (int)_parseOptions.BatchForKafka!;
                    int offset = 0;

                    while (!_cancellationToken.IsCancellationRequested)
                    {
                        var part = messages.Skip(offset).Take(batchSize).ToList();
                        if (part.Count == 0)
                        {
                            flagExit = true;
                            break;
                        }
                        MessagesForWrite messagesForWrite = new()
                        {
                            ChatId = chatId.ToString(),
                            TopicName = _topicsKafka.GetTopic(TopicResolve.Messages),
                            Messages = part,
                            ModesForDB = [ModesForDB.INSERT_IGNORE]
                        };
                        if (messagesForWrite.Messages.Count > 0)
                        {
                            await _kafkaSendMessage(messagesForWrite);
                        }
                        offset += part.Count;
                    }
                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = "Func [WriteChannelMessages]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                    await Task.Delay(60_000, cancellationToken: _cancellationToken);
                }
            }
        }

        public async Task WriteMessagesProperties(List<MessagesPropertiesDTO> messagesProp, long chatId)
        {
            bool flagExit = false;
            while (!flagExit && !_cancellationToken.IsCancellationRequested)
            {
                try
                {
                    int batchSize = (int)_parseOptions.BatchForKafka!;
                    int offset = 0;

                    while (!_cancellationToken.IsCancellationRequested)
                    {
                        var part = messagesProp.Skip(offset).Take(batchSize).ToList();
                        if (part.Count == 0)
                        {
                            flagExit = true;
                            break;
                        }
                        MessagesPropertiesForWrite messagesPropForWrite = new()
                        {
                            ChatId = chatId.ToString(),
                            TopicName = _topicsKafka.GetTopic(TopicResolve.Messages_Properties),
                            MessagesProp = part,
                            ModesForDB = [ModesForDB.INSERT_IGNORE]
                        };
                        if (messagesPropForWrite.MessagesProp.Count > 0)
                        {
                            await _kafkaSendMessage(messagesPropForWrite);
                        }
                        offset += part.Count;
                    }
                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = "Func [WriteMessagesProperties]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                    await Task.Delay(60_000, cancellationToken: _cancellationToken);
                }
            }
        }

        public async Task WriteMessageEntities(List<MessagesEntitiesDTO> messagesEntities, long chatId)
        {
            bool flagExit = false;
            while (!flagExit && !_cancellationToken.IsCancellationRequested)
            {
                try
                {
                    int batchSize = (int)_parseOptions.BatchForKafka!;
                    int offset = 0;

                    while (!_cancellationToken.IsCancellationRequested)
                    {
                        var part = messagesEntities.Skip(offset).Take(batchSize).ToList();
                        if (part.Count == 0)
                        {
                            flagExit = true;
                            break;
                        }
                        MessagesEntitiesForWrite messagesEntitiesForWrite = new()
                        {
                            ChatId = chatId.ToString(),
                            TopicName = _topicsKafka.GetTopic(TopicResolve.Messages_Entities),
                            MessagesEntities = part,
                            ModesForDB = [ModesForDB.INSERT_IGNORE]
                        };
                        if (messagesEntitiesForWrite.MessagesEntities.Count > 0)
                        {
                            await _kafkaSendMessage(messagesEntitiesForWrite);
                        }
                        offset += part.Count;
                    }
                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = "Func [WriteMessageEntities]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                    await Task.Delay(60_000, cancellationToken: _cancellationToken);
                }
            }
        }

        public async Task WriteReactions(List<ReactionDTO> reactions, long chatId)
        {
            bool flagExit = false;
            while (!flagExit && !_cancellationToken.IsCancellationRequested)
            {
                try
                {
                    int batchSize = (int)_parseOptions.BatchForKafka!;
                    int offset = 0;

                    while (!_cancellationToken.IsCancellationRequested)
                    {
                        var part = reactions.Skip(offset).Take(batchSize).ToList();
                        if (part.Count == 0)
                        {
                            flagExit = true;
                            break;
                        }
                        ReactionsForWrite reactionsForWrite = new()
                        {
                            ChatId = chatId.ToString(),
                            TopicName = _topicsKafka.GetTopic(TopicResolve.Reactions),
                            Reactions = part,
                            ModesForDB = [ModesForDB.INSERT_IGNORE]
                        };
                        if (reactionsForWrite.Reactions.Count > 0)
                        {
                            await _kafkaSendMessage(reactionsForWrite);
                        }
                        offset += part.Count;
                    }
                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = "Func [WriteReactions]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                    await Task.Delay(60_000, cancellationToken: _cancellationToken);
                }
            }
        }

        public async Task WriteReactionsGeneral(List<ReactionGeneralDTO> reactionsGeneral, long chatId)
        {
            bool flagExit = false;
            while (!flagExit && !_cancellationToken.IsCancellationRequested)
            {
                try
                {
                    int batchSize = (int)_parseOptions.BatchForKafka!;
                    int offset = 0;

                    while (!_cancellationToken.IsCancellationRequested)
                    {
                        var part = reactionsGeneral.Skip(offset).Take(batchSize).ToList();
                        if (part.Count == 0)
                        {
                            flagExit = true;
                            break;
                        }
                        ReactionsGeneralForWrite reactionsGeneralForWrite = new()
                        {
                            ChatId = chatId.ToString(),
                            TopicName = _topicsKafka.GetTopic(TopicResolve.Reactions_General),
                            ReactionsGeneral = part,
                            ModesForDB = [ModesForDB.INSERT_IGNORE]
                        };
                        if (reactionsGeneralForWrite.ReactionsGeneral.Count > 0)
                        {
                            await _kafkaSendMessage(reactionsGeneralForWrite);
                        }
                        offset += part.Count;
                    }
                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = "Func [WriteReactionsGeneral]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                    await Task.Delay(60_000, cancellationToken: _cancellationToken);
                }
            }
        }

        public async Task WriteGifts(List<GiftsDTO> gifts, long chatId)
        {
            bool flagExit = false;
            while (!flagExit && !_cancellationToken.IsCancellationRequested)
            {
                try
                {
                    int batchSize = (int)_parseOptions.BatchForKafka!;
                    int offset = 0;

                    while (!_cancellationToken.IsCancellationRequested)
                    {
                        var part = gifts.Skip(offset).Take(batchSize).ToList();
                        if (part.Count == 0)
                        {
                            flagExit = true;
                            break;
                        }
                        GiftsForWrite giftsForWrite = new()
                        {
                            ChatId = chatId.ToString(),
                            TopicName = _topicsKafka.GetTopic(TopicResolve.Gifts),
                            Gifts = part,
                            ModesForDB = [ModesForDB.INSERT_IGNORE]
                        };
                        if (giftsForWrite.Gifts.Count > 0)
                        {
                            await _kafkaSendMessage(giftsForWrite);
                        }
                        offset += part.Count;
                    }
                }
                catch (Exception ex)
                {
                    _logger.Log(new Log()
                    {
                        Message = "Func [WriteGifts]" + "\nError: " + ex.Message + "\n" + ex.InnerException,
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Error
                    });
                    await Task.Delay(60_000, cancellationToken: _cancellationToken);
                }
            }
        }

        public async Task WriterParseMessage(ParseMessageStorage storage, long chat_id)
        {
            _logger.Log(new Log()
            {
                Message = $"Началась отправка пользователей...",
                LogDT = DateTime.UtcNow,
                Level = Levels.Info
            });
            await WriteUserAndChatAsync(storage.sendParticipant, chat_id);
            _logger.Log(new Log()
            {
                Message = $"Отправка юзеров окончена [{storage.sendParticipant.Count}], началась отправка сообщений...",
                LogDT = DateTime.UtcNow,
                Level = Levels.Info
            });
            await WriteMessages(storage.sendMessage, chatId: chat_id);
            _logger.Log(new Log()
            {
                Message = $"Отправка сообщений окончена [{storage.sendMessage.Count}], началась отправка реакций...",
                LogDT = DateTime.UtcNow,
                Level = Levels.Info
            });
            await WriteReactions(storage.sendReactions, chat_id);
            _logger.Log(new Log()
            {
                Message = $"Отправка реакций окончена [{storage.sendReactions.Count}], началась отправка реакций(general)...",
                LogDT = DateTime.UtcNow,
                Level = Levels.Info
            });
            await WriteReactionsGeneral(storage.sendReactionsGeneral, chatId: chat_id);
            _logger.Log(new Log()
            {
                Message = $"Отправка реакций(general) окончена [{storage.sendReactionsGeneral.Count}], началась отправка entities...",
                LogDT = DateTime.UtcNow,
                Level = Levels.Info
            });
            await WriteMessageEntities(storage.sendMessageEntities, chat_id);
            _logger.Log(new Log()
            {
                Message = $"Отправка entities окончена [{storage.sendMessageEntities.Count}], началась отправка properties...",
                LogDT = DateTime.UtcNow,
                Level = Levels.Info
            });
            await WriteMessagesProperties(storage.sendMessagesProperties, chat_id);
            _logger.Log(new Log()
            {
                Message = $"Отправка properties окончена [{storage.sendMessagesProperties.Count}].",
                LogDT = DateTime.UtcNow,
                Level = Levels.Info
            });
        }
    }
}