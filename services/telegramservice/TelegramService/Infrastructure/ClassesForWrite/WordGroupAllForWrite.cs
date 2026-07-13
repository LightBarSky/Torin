using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Parse;
using DTOs.DTO;

namespace TelegramService.Infrastructure.ClassesForWrite
{
    public class WordGroupAllForWrite: IKafkaMessageCreator
    {
        public required string ChatId { get; set; }
        public required string TopicName { get; set; }
        public List<WordGroupAllDTO> WordGroupAlls;
        public List<ModesForDB> ModesForDB { get; set; }

        public WordGroupAllForWrite()
        {
            WordGroupAlls = new();
            ModesForDB = new();
        }

        public WordGroupAllForWrite(int ensureCapacity): this()
        {
            WordGroupAlls.EnsureCapacity(ensureCapacity);
        }
        
        public (string type, string key, string mode, string serializeObjects) CreateQuery()
        {
            var serializeObjects = TelegramHelper.SerialezeObjects(WordGroupAlls);
            return (TopicName, ChatId, string.Join(" ", ModesForDB), serializeObjects);
        }
    }
}