using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Contracts.Parse;
using DTOs.DTO;
using Microsoft.AspNetCore.SignalR;
using Newtonsoft.Json;
using TelegramService;
using TelegramService.Infrastructure;
using Serilog;
using Microsoft.Extensions.Options;
using Torin.Api.Settings;
using Contracts.Infrastructure;
using TelegramService.Contract;
using TelegramService.Setting;
using Torin.Api.Infrastructure;
using TL;

namespace Torin.Api.Services
{
    public interface IHandlerStatusService
    {
        Task SetStatusAsync(long handlerId, Status status);
        List<long> GetRunning();
    }
    public class HandlerStatusService : IHandlerStatusService
    {
        private readonly ConcurrentDictionary<long, IParse> _parseStorage = new();
        private readonly Dictionary<string, string> _topics = new();
        private readonly IWebHostEnvironment _env;
        private readonly KafkaProducerService _kafkaService;
        private readonly ParseGroupOptions _parseOptions;
        private readonly IConfiguration _configuration;
        private readonly IHttpClientFactory _httpClientFactory;
        private readonly HttpClient _httpClientForParser;
        private readonly HttpClient _httpClientForGui;
        private readonly string _topicForLogs;
        private readonly string _sessionsPath;
        private readonly string _accessHashesPath;
        private EventHandler<Contracts.Infrastructure.Log>? _logsSenderHandler;
        private readonly ConcurrentDictionary<string, SemaphoreSlim> _locks = new();

        public HandlerStatusService(KafkaProducerService kafkaService,
        IWebHostEnvironment env, IConfiguration configuration, IHttpClientFactory httpClientFactory, IOptions<KafkaTopicsOptions> options,
        IOptions<ParseGroupOptions> parseOptions)
        {
            _kafkaService = kafkaService;
            _env = env;
            _configuration = configuration;
            _httpClientFactory = httpClientFactory;
            _httpClientForParser = _httpClientFactory.CreateClient("dbService");
            _httpClientForGui = _httpClientFactory.CreateClient("guiService");
            _topicForLogs = _configuration["kafka:Topics:logs_handlers"] ?? "logs_handlers";
            _topics = options.Value.Topics;
            _parseOptions = parseOptions.Value;
            _sessionsPath = _configuration["Paths:Sessions"] ?? Path.Combine(_env.ContentRootPath, "AppData", "sessions");
            _accessHashesPath = _configuration["Paths:AccessHashes"] ?? Path.Combine(_env.ContentRootPath, "AppData", "AccessHashes");
            _logsSenderHandler = async (sender, e) =>
            {
                await LogsSender(sender, e);
            };
        }

        public async Task SetStatusAsync(long handlerId, Status status)
        {
            if (status == Status.STARTED)
            {
                await Running(handlerId);
            }
            else if (status == Status.STOPPED)
            {
                await Stopping(handlerId);
            }
            else if (status == Status.RESTARTING)
            {
                await Restarting(handlerId);
            }
        }

        public List<long> GetRunning() => _parseStorage.Select(x => x.Key).ToList();

        private async Task Running(long handlerId)
        {
            if (_parseStorage.ContainsKey(handlerId)) return;
            var handler = await _httpClientForParser.GetFromJsonAsync<HandlerDTO>($"/api/v1/handlers/{handlerId}");
            if (handler is null)
            {
                throw new Exception($"Обработчик с таким id [{handlerId}] не найден!");
            }
            if (handler.Category == "ParseGroup")
            {
                var parseGroup = InitializeParse(handler);
                _parseStorage.AddOrUpdate(handlerId, parseGroup, (id, old_val) => parseGroup);
                await _parseStorage[handlerId].Start();
            }
        }

        private async Task Stopping(long handlerId)
        {
            if (_parseStorage.ContainsKey(handlerId))
            {
                await _parseStorage[handlerId].Stop();
                _parseStorage[handlerId].GetLogger().LogEvent -= _logsSenderHandler;
                _parseStorage.Remove(handlerId, out _);
            }
        }

        private async Task Restarting(long handlerId)
        {
            await Stopping(handlerId);
            await Running(handlerId);
        }

        private IParse InitializeParse(HandlerDTO handler)
        {
            Func<string, string?> config = new Func<string, string?>(x =>
            {
                return x switch
                {
                    "api_id" => handler.ApiId.ToString(),
                    "api_hash" => handler.Hash,
                    "phone_number" => handler.Phone,
                    "session_pathname" => Path.Combine(_sessionsPath,
                    handler.Phone + ".session"),
                    "verification_code" => throw new Exception("verification_code"),
                    "password" => throw new Exception("password"),
                    _ => null
                };
            });

            IParseTelegram parseTelegram = IParseTelegram.Builder()
            .SetConfig(config)
            .SetKafkaSendMessage(KafkaSendMessageToTopic)
            .SetHandlerId(handler.Id)
            .SetPathAccessHash(_accessHashesPath)
            .SetHttpClient(_httpClientForParser)
            .SetKafkaTopics(_topics)
            .SetParseOptions(_parseOptions).Build();

            parseTelegram.GetLogger().LogEvent += _logsSenderHandler;

            return parseTelegram;
        }

        private async Task KafkaSendMessageToTopic(IKafkaMessageCreator mess)
        {
            var createMess = mess.CreateQuery();
            if (createMess.type is not null && createMess.serializeObjects is not null)
            {
                await _kafkaService.ProduceAsync(createMess.type, createMess.key,
                new KafkaObjectDTO(mode: createMess.mode, serializeObjects: createMess.serializeObjects).Serialize());
            }
        }

        private async Task LogsSender(object? sender, Contracts.Infrastructure.Log e)
        {
            if (sender is Contracts.Parse.ILogger logger)
            {
                LogEntryDTO log = new LogEntryDTO()
                {
                    HandlerId = logger.GetHandlerId().ToString(),
                    Message = $"[{e.Level}] {e.Message}",
                    FormatterTimestamp = $"{e.LogDT:yyyy-MM-dd HH:mm:ss}",
                    Timestamp = e.LogDT,
                    Level = e.Level.ToString()
                };
                var semaphore = _locks.GetOrAdd(logger.GetHandlerId().ToString(), _ => new SemaphoreSlim(1, 1));

                await semaphore.WaitAsync();
                try
                {
                    await _kafkaService.ProduceAsync(_topicForLogs, logger.GetHandlerId().ToString(), JsonConvert.SerializeObject(log));
                }
                finally
                {
                    semaphore.Release();
                }
                if (e.Level == Levels.FatalError)
                {
                    await SetStatusAsync(logger.GetHandlerId(), Status.STOPPED);
                    var content = new StringContent(JsonConvert.SerializeObject(log), Encoding.UTF8, "application/json");
                    try
                    {
                        await _httpClientForGui.PostAsync($"/api/v1/notify/fatal-log", content);
                    }
                    catch (Exception ex)
                    {
                        Serilog.Log.Error($"/api/v1/notify/fatal-log failed: {ex.Message}");
                    }
                }
                if (e.Command is not null && e.Command == Commands.Restart)
                {
                    await SetStatusAsync(logger.GetHandlerId(), Status.RESTARTING);
                }
            }
        }
    }
}