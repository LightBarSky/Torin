using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Parse;
using TL;
using DTOs.DTO;

namespace TelegramService.Infrastructure.ClassesForWrite
{
    public class UsersForWriteOrUpdate : IKafkaMessageCreator
    {
        public required string ChatId { get; set; }
        public required string TopicName { get; set; }
        public List<UserDTO> Users;
        public List<ModesForDB> ModesForDB { get; set; }

        public UsersForWriteOrUpdate()
        {
            Users = new();
            ModesForDB = new();
        }

        public UsersForWriteOrUpdate(int ensureCapacity): this()
        {
            Users.EnsureCapacity(ensureCapacity);
        }
        
        public (string type, string key, string mode, string serializeObjects) CreateQuery()
        {
            var serializeObjects = TelegramHelper.SerialezeObjects(Users);
            return (TopicName, ChatId, string.Join(" ", ModesForDB), serializeObjects);
        }
    }
}