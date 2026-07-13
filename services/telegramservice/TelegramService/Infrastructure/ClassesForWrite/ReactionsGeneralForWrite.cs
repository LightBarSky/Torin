using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Parse;
using DTOs.DTO;

namespace TelegramService.Infrastructure.ClassesForWrite
{
    public class ReactionsGeneralForWrite : IKafkaMessageCreator
    {
        public required string ChatId { get; set; }
        public required string TopicName { get; set; }
        public List<ReactionGeneralDTO> ReactionsGeneral { get; set; }
        public List<ModesForDB> ModesForDB { get; set; }

        public ReactionsGeneralForWrite()
        {
            ReactionsGeneral = new();
            ModesForDB = new();
        }

        public ReactionsGeneralForWrite(int ensureCapacity): this()
        {
            ReactionsGeneral.EnsureCapacity(ensureCapacity);
        }

        public (string type, string key, string mode, string serializeObjects) CreateQuery()
        {
            var serializeObjects = TelegramHelper.SerialezeObjects<ReactionGeneralDTO>(ReactionsGeneral);
            return (TopicName, ChatId, string.Join(" ", ModesForDB), serializeObjects);
        }
    }
}