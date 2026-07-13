using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Infrastructure;
using Contracts.Parse;
using TelegramService.Contract;
using TelegramService.Setting;
using static TelegramService.Contract.IParseTelegram;

namespace TelegramService.Parse
{
    public class DefaultParseTelegramBulder : IBuilder
    {
        private Func<string, string?>? _config;
        private Func<IKafkaMessageCreator, Task>? _kafkaSendMessage;
        private long? _handlerId;
        private string? _pathDirAccessHash;
        private HttpClient? _httpClient;
        private Dictionary<string, string>? _topics;
        private ParseGroupOptions? _parseOptions;

        public IBuilder SetConfig(Func<string, string?> config)
        {
            _config = config;
            return this;
        }

        public IBuilder SetKafkaSendMessage(Func<IKafkaMessageCreator, Task> kafkaSendMessage)
        {
            _kafkaSendMessage = kafkaSendMessage;
            return this;
        }

        public IBuilder SetHandlerId(long handlerId)
        {
            _handlerId = handlerId;
            return this;
        }

        public IBuilder SetPathAccessHash(string pathDirAccessHash)
        {
            _pathDirAccessHash = pathDirAccessHash;
            return this;
        }

        public IBuilder SetHttpClient(HttpClient httpClient)
        {
            _httpClient = httpClient;
            return this;
        }

        public IBuilder SetKafkaTopics(Dictionary<string, string> topics)
        {
            _topics = topics;
            return this;
        }

        public IBuilder SetParseOptions(ParseGroupOptions parseOptions)
        {
            _parseOptions = parseOptions;
            return this;
        }

        public IParseTelegram Build()
        {
            if (_config is null)
            {
                throw new Exception("Config for Parse not must be is null");
            }
            if (_kafkaSendMessage is null)
            {
                throw new Exception("KafkaSendMessage for Parse not must be is null");
            }
            if (!_handlerId.HasValue)
            {
                throw new Exception("HandlerId for Parse not must be is null");
            }
            if (string.IsNullOrEmpty(_pathDirAccessHash))
            {
                throw new Exception("PathDirAccessHash for Parse not must be is null");
            }
            if (_httpClient is null)
            {
                throw new Exception("HttpClient for Parse not must be is null");
            }
            if (_topics is null)
            {
                throw new Exception("Topics for Parse not must be is null");
            }
            if (_parseOptions is null)
            {
                throw new Exception("HttpClient for Parse not must be is null");
            }
            ILogger logger = new Logger((long)_handlerId);
            var parseTelegramCore = new ParseTelegramCore(_config, _kafkaSendMessage, _topics, (long)_handlerId, logger,
             _parseOptions, _pathDirAccessHash, _httpClient);
            return new ParseGroupGeneral((long)_handlerId, _parseOptions.DelayRestartM, logger, parseTelegramCore);
        }
    }
}