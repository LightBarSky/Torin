using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Parse;
using DTOs.DTO;

namespace TelegramService.Infrastructure.ClassesForWrite
{
    public class ReactionsForWrite : IKafkaMessageCreator
    {
        public required string ChatId { get; set; }
        public required string TopicName { get; set; }
        public List<ReactionDTO> Reactions { get; set; }
        public List<ModesForDB> ModesForDB { get; set; }

        public ReactionsForWrite()
        {
            Reactions = new();
            ModesForDB = new();
        }

        public ReactionsForWrite(int ensureCapacity): this()
        {
            Reactions.EnsureCapacity(ensureCapacity);
        }

        public (string type, string key, string mode, string serializeObjects) CreateQuery()
        {
            var serializeObjects = TelegramHelper.SerialezeObjects<ReactionDTO>(Reactions);
            return (TopicName, ChatId, string.Join(" ", ModesForDB), serializeObjects);
        }
    }
}