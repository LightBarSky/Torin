using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Parse;
using DTOs.DTO;

namespace TelegramService.Infrastructure.ClassesForWrite
{
    public class MessagesEntitiesForWrite : IKafkaMessageCreator
    {
        public required string ChatId { get; set; }
        public required string TopicName { get; set; }
        public List<MessagesEntitiesDTO> MessagesEntities { get; set; }
        public List<ModesForDB> ModesForDB { get; set; }

        public MessagesEntitiesForWrite()
        {
            MessagesEntities = new();
            ModesForDB = new();
        }

        public MessagesEntitiesForWrite(int ensureCapacity) : this()
        {
            MessagesEntities.EnsureCapacity(ensureCapacity);
        }

        public (string type, string key, string mode, string serializeObjects) CreateQuery()
        {
            var serializeObjects = TelegramHelper.SerialezeObjects<MessagesEntitiesDTO>(MessagesEntities);
            return (TopicName, ChatId, string.Join(" ", ModesForDB), serializeObjects);
        }
    }
}