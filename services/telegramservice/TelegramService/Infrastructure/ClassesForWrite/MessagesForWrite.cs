using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Parse;
using DTOs.DTO;

namespace TelegramService.Infrastructure.ClassesForWrite
{
    public class MessagesForWrite: IKafkaMessageCreator
    {
        public required string ChatId { get; set; }
        public required string TopicName { get; set; }
        public List<MessageDTO> Messages { get; set; }
        public List<ModesForDB> ModesForDB { get; set; }

        public MessagesForWrite()
        {
            Messages = new();
            ModesForDB = new();
        }

        public MessagesForWrite(int ensureCapacity): this()
        {
            Messages.EnsureCapacity(ensureCapacity);
        }

        public (string type, string key, string mode, string serializeObjects) CreateQuery()
        {
            var serializeObjects = TelegramHelper.SerialezeObjects<MessageDTO>(Messages);
            return (TopicName, ChatId, string.Join(" ", ModesForDB), serializeObjects);
        }
    }
}