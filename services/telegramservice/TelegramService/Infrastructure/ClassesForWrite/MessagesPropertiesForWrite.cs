using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Parse;
using DTOs.DTO;

namespace TelegramService.Infrastructure.ClassesForWrite
{
    public class MessagesPropertiesForWrite : IKafkaMessageCreator
    {
        public required string ChatId { get; set; }
        public required string TopicName { get; set; }
        public List<MessagesPropertiesDTO> MessagesProp { get; set; }
        public List<ModesForDB> ModesForDB { get; set; }

        public MessagesPropertiesForWrite()
        {
            MessagesProp = new();
            ModesForDB = new();
        }

        public MessagesPropertiesForWrite(int ensureCapacity) : this()
        {
            MessagesProp.EnsureCapacity(ensureCapacity);
        }

        public (string type, string key, string mode, string serializeObjects) CreateQuery()
        {
            var serializeObjects = TelegramHelper.SerialezeObjects<MessagesPropertiesDTO>(MessagesProp);
            return (TopicName, ChatId, string.Join(" ", ModesForDB), serializeObjects);
        }
    }
}