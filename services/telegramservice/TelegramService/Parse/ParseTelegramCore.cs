using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Infrastructure;
using Contracts.Parse;
using DTOs.DTO;
using TelegramService.Infrastructure;
using TelegramService.Setting;
using TL;

namespace TelegramService.Parse
{
    internal class ParseTelegramCore : IAsyncDisposable
    {
        private ClientBase _clientBase;
        private TopicsKafka _topicsKafka;
        private ParseGroupOptions _parseOptions;
        private CancellationTokenSource _cancellationTokenSource;
        private ConcurrentQueue<ChatBase> _chatBaseCache;
        private string _pathToDirAccessHash;
        private string? _pathToFileAccessHash = null;
        private string? _pathToDirMedia = null;
        private string? _pathToDirProfilePhoto = null;
        private HandlerDTO? _handlerDTO = null;
        private SavedState _savedState;
        private ApiClient _apiClient;
        private ConcurrentDictionary<long, byte> _detectChannelPrivateDict;
        private EventHandler<Log>? _clientLogHandler;
        private readonly long _handlerId;
        private readonly List<Task> _tasksOfHandler;


        public ClientBase ClientBase => _clientBase;
        public TopicsKafka TopicsKafka => _topicsKafka;
        public long HandlerId => _handlerId;
        public CancellationToken CancellationToken => _cancellationTokenSource.Token;
        public ParseGroupOptions ParseOptions => _parseOptions;
        public ApiClient ApiClient => _apiClient;
        public readonly Func<IKafkaMessageCreator, Task> KafkaSendMessage;
        public readonly ILogger Logger;

        public ParseTelegramCore(Func<string, string?> config, Func<IKafkaMessageCreator, Task> kafkaSendMessage, Dictionary<string, string> topics,
        long handlerId, ILogger logger, ParseGroupOptions parseOptions, string pathToDirAccessHash, HttpClient httpClient)
        {
            KafkaSendMessage = kafkaSendMessage;
            Logger = logger;

            _clientLogHandler = (sender, e) =>
            {
                Logger.Log(e);
            };

            _parseOptions = parseOptions;
            _handlerId = handlerId;
            _cancellationTokenSource = new CancellationTokenSource();
            _clientBase = new ClientBase(config);
            _clientBase.ClientLogEvent += _clientLogHandler;
            _topicsKafka = new TopicsKafka(topics);
            _apiClient = new ApiClient(httpClient);
            _savedState = new SavedState();
            _chatBaseCache = new ConcurrentQueue<ChatBase>();
            _pathToDirAccessHash = pathToDirAccessHash;
            _detectChannelPrivateDict = new ConcurrentDictionary<long, byte>();
            _tasksOfHandler = new List<Task>();
        }



        //=================== Queue ======================================

        public void AddChatQueue(ChatBase chatBase)
        {
            _chatBaseCache.Enqueue(chatBase);
        }

        public bool IsContainsQueue(long idGroup)
        {
            return _chatBaseCache.Select(x => x.ID).Contains(idGroup);
        }

        //================== Saved State =================================

        public async Task CollectReadHashesAsync()
        {
            if (string.IsNullOrEmpty(_pathToFileAccessHash))
            {
                Logger.Log(new Log()
                {
                    Message = $"Func [CollectReadHashesAsync] PathToAccessHash not must is null!",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Warning
                });
                return;
            }
            try
            {
                using (var file = File.Open(_pathToFileAccessHash, FileMode.OpenOrCreate))
                {
                    var saved_temp = await System.Text.Json.JsonSerializer.DeserializeAsync<SavedState>(file);
                    if (saved_temp is not null)
                    {
                        _savedState = saved_temp;
                    }
                }
                Logger.Log(new Log()
                {
                    Message = $"AccessHashes успешно прочитан, всего состояний: [Channels = {_savedState.Channels.Count}, Users = {_savedState.Users.Count}]",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Info
                });
            }
            catch (Exception ex)
            {
                Logger.Log(new Log()
                {
                    Message = $"Func [CollectReadHashesAsync] [message: {ex.Message}] [innerException: {ex.InnerException?.Message}]",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Error
                });
            }
        }

        public async Task CollectWriteHashesAsync()
        {
            if (string.IsNullOrEmpty(_pathToFileAccessHash))
            {
                Logger.Log(new Log()
                {
                    Message = $"Func [CollectWriteHashesAsync] PathToAccessHash not must is null!",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Warning
                });
                return;
            }
            try
            {
                using (var stateStream = File.Create(_pathToFileAccessHash))
                    await System.Text.Json.JsonSerializer.SerializeAsync(stateStream, _savedState);
                Logger.Log(new Log()
                {
                    Message = $"Func [CollectWriteHashesAsync] Успешная запись SavedState! [Channels = {_savedState.Channels.Count}, Users = {_savedState.Users.Count}]",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Info
                });
            }
            catch (Exception ex)
            {
                Logger.Log(new Log()
                {
                    Message = $"Func [CollectWriteHashesAsync] [message: {ex.Message}] [innerException: {ex.InnerException?.Message}]",
                    LogDT = DateTime.UtcNow,
                    Level = Levels.Error
                });
            }
        }

        public void AddOrUpdateChannels(long id, long access_hash)
        {
            _savedState.Channels.AddOrUpdate(id, x => access_hash, (id, new_val) => new_val);
        }

        public bool RemoveFromSavedState(long idGroup)
        {
            return _savedState.Channels.Remove(idGroup, out _);
        }

        public bool TryDequeue(out ChatBase? chatBase)
        {
            return _chatBaseCache.TryDequeue(out chatBase);
        }

        public KeyValuePair<long, long> GetByIdGroupSavedState(long idGroup)
        {
            return _savedState.Channels.FirstOrDefault(x => x.Key == idGroup);
        }

        //============================= Detect channel Private =======================

        public bool ContainsDetectChannelPrivate(long idChat)
        {
            return _detectChannelPrivateDict.ContainsKey(idChat);
        }

        public bool TryAddDetectChannelPrivate(long idChat)
        {
            return _detectChannelPrivateDict.TryAdd(idChat, 0);
        }

        //============================== Parse =======================================
        public bool isParseMessage()
        {
            if ((!(bool)_parseOptions.ParseMessageOnEntities! && !(bool)_parseOptions.ParseMessageOnMessages! &&
            !(bool)_parseOptions.ParseMessageOnProperties! && !(bool)_parseOptions.ParseMessageOnReactions! &&
            !(bool)_parseOptions.ParseMessageOnReactionsGeneral! && !(bool)_parseOptions.ParseMessageOnUsers!) ||
            !(bool)_parseOptions.ParseMessage!)
            {
                return false;
            }
            return true;
        }

        //============================== General =======================================
        public async Task PreStart()
        {
            var fullNameUser = await _clientBase.Login();
            Logger.Log(new Log()
            {
                Message = $"Вы вошли как: {fullNameUser}",
                LogDT = DateTime.UtcNow,
                Level = Levels.Info
            });

            _handlerDTO = await _apiClient.GetHandlersById(_handlerId);
            if (_handlerDTO is null)
            {
                throw new Exception(message: $"Handler с id-{_handlerId} не найден");
            }

            if (!string.IsNullOrEmpty(_pathToDirAccessHash))
            {
                if (!Directory.Exists(_pathToDirAccessHash))
                {
                    throw new Exception(message: $"Не существует директории к хэшам: [PathDirAccessHash = {_pathToDirAccessHash}]");
                }
                _pathToFileAccessHash = Path.Combine(_pathToDirAccessHash, _handlerDTO.Phone + "_" + _handlerDTO.Id + ".json");
                await CollectReadHashesAsync();
            }
            else
            {
                throw new Exception(message: $"Один из путей не найден: [AccessHash = {_pathToFileAccessHash}] [RootPath = {_pathToDirAccessHash}]");
            }

            if (!string.IsNullOrEmpty(_handlerDTO.DirectoryForMedia))
            {
                if (Directory.Exists(_handlerDTO.DirectoryForMedia))
                {
                    _pathToDirMedia = _handlerDTO.DirectoryForMedia;
                }
                else
                {
                    Logger.Log(new Log()
                    {
                        Message = $"Директория для медиа [DirectoryForMedia = {_handlerDTO.DirectoryForMedia}] не существует",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Warning
                    });
                }
            }
            if (!string.IsNullOrEmpty(_handlerDTO.DirectoryForUserPhoto))
            {
                if (Directory.Exists(_handlerDTO.DirectoryForUserPhoto))
                {
                    _pathToDirProfilePhoto = _handlerDTO.DirectoryForUserPhoto;
                }
                else
                {
                    Logger.Log(new Log()
                    {
                        Message = $"Директория для фото пользователей [DirectoryForUserPhoto = {_handlerDTO.DirectoryForUserPhoto}] не существует",
                        LogDT = DateTime.UtcNow,
                        Level = Levels.Warning
                    });
                }
            }
            _topicsKafka.TryAnyTopic();
        }

        //============================== Tasks =======================================
        public void AddTask(Func<Task> action)
        {
            _tasksOfHandler.Add(Task.Run(action));
        }

        public async ValueTask DisposeAsync()
        {
            _cancellationTokenSource.Cancel();
            if (_clientLogHandler is not null)
            {
                _clientBase.ClientLogEvent -= _clientLogHandler;
            }
            await _clientBase.DisposeAsync();
            await Task.WhenAll(_tasksOfHandler);
            await CollectWriteHashesAsync();
            _chatBaseCache.Clear();
            _savedState.Clear();
        }
    }
}