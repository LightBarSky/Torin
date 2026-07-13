using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Parse;
using DTOs.DTO;

namespace TelegramService.Infrastructure.ClassesForWrite
{
    public class AdminChatsForWrite: IKafkaMessageCreator
    {
        public required string ChatId { get; set; }
        public required string TopicName { get; set; }
        public List<AdminChatDTO> AdminChats { get; set; }
        public List<ModesForDB> ModesForDB { get; set; }

        public AdminChatsForWrite()
        {
            AdminChats = new();
            ModesForDB = new();
        }

        public AdminChatsForWrite(int ensureCapacity): this()
        {
            AdminChats.EnsureCapacity(ensureCapacity);
        }

        public (string type, string key, string mode, string serializeObjects) CreateQuery()
        {
            var serializeObjects = TelegramHelper.SerialezeObjects<AdminChatDTO>(AdminChats);
            return (TopicName, ChatId, string.Join(" ", ModesForDB), serializeObjects);
        }
    }
}