using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Parse;
using DTOs.DTO;

namespace TelegramService.Infrastructure.ClassesForWrite
{
    public class TaskChatsForWrite : IKafkaMessageCreator
    {
        public required string ChatId { get; set; }
        public required string TopicName { get; set; }
        public List<TaskChatDTO> TaskChats { get; set; }
        public List<ModesForDB> ModesForDB { get; set; }

        public TaskChatsForWrite()
        {
            this.TaskChats = new();
            ModesForDB = new();
        }

        public TaskChatsForWrite(int ensureCapacity): this()
        {
            this.TaskChats.EnsureCapacity(ensureCapacity);
        }

        public (string type, string key, string mode, string serializeObjects) CreateQuery()
        {
            var serializeObjects = TelegramHelper.SerialezeObjects<TaskChatDTO>(TaskChats);
            return (TopicName, ChatId, string.Join(" ", ModesForDB), serializeObjects);
        }
    }
}