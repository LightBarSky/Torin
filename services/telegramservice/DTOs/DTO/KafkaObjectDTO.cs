using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace DTOs.DTO
{
    public class KafkaObjectDTO
    {
        public string? Mode { get; set; }
        public string? SerializeObjects { get; set; }

        public KafkaObjectDTO(string? mode, string? serializeObjects)
        {
            this.Mode = mode;
            this.SerializeObjects = serializeObjects;
        }

        public string Serialize()
        {
            return JsonConvert.SerializeObject(this);
        }
    }
}