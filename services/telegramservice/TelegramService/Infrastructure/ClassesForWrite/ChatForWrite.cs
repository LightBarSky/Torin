using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Parse;
using DTOs.DTO;
using TL;

namespace TelegramService.Infrastructure.ClassesForWrite
{
    public class ChatForWrite: IKafkaMessageCreator
    {
        public required string ChatId { get; set; }
        public required string TopicName { get; set; }
        public List<ChatDTO> Chats { get; set; }
        public List<ModesForDB> ModesForDB { get; set; }

        public ChatForWrite()
        {
            Chats = new();
            ModesForDB = new();
        }

        public ChatForWrite(int ensureCapacity): this()
        {
            Chats.EnsureCapacity(ensureCapacity);
        }

        public (string type, string key, string mode, string serializeObjects) CreateQuery()
        {
            var serializeObjects = TelegramHelper.SerialezeObjects<ChatDTO>(Chats);
            return (TopicName, ChatId, string.Join(" ", ModesForDB), serializeObjects);
        }
    }
}