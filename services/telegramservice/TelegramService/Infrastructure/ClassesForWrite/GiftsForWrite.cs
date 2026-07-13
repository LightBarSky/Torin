using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Contracts.Parse;
using DTOs.DTO;

namespace TelegramService.Infrastructure.ClassesForWrite
{
    public class GiftsForWrite : IKafkaMessageCreator
    {
        public required string ChatId { get; set; }
        public required string TopicName { get; set; }
        public List<GiftsDTO> Gifts { get; set; }
        public List<ModesForDB> ModesForDB { get; set; }

        public GiftsForWrite()
        {
            Gifts = new();
            ModesForDB = new();
        }

        public GiftsForWrite(int ensureCapacity): this()
        {
            Gifts.EnsureCapacity(ensureCapacity);
        }

        public (string type, string key, string mode, string serializeObjects) CreateQuery()
        {
            var serializeObjects = TelegramHelper.SerialezeObjects<GiftsDTO>(Gifts);
            return (TopicName, ChatId, string.Join(" ", ModesForDB), serializeObjects);
        }
    }
}