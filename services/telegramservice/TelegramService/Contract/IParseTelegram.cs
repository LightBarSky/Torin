using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Parse;
using TelegramService.Parse;
using TelegramService.Setting;

namespace TelegramService.Contract
{
    public interface IParseTelegram : IParse
    {
        static IBuilder Builder()
        {
            return new DefaultParseTelegramBulder();
        }

        interface IBuilder
        {
            IBuilder SetConfig(Func<string, string?> config);
            IBuilder SetKafkaSendMessage(Func<IKafkaMessageCreator, Task> kafkaSendMessage);
            IBuilder SetHandlerId(long handlerId);
            IBuilder SetPathAccessHash(string pathDirAccessHash);
            IBuilder SetHttpClient(HttpClient httpClient);
            IBuilder SetKafkaTopics(Dictionary<string, string> topics);
            IBuilder SetParseOptions(ParseGroupOptions parseOptions);
            IParseTelegram Build();
        }
    }
}