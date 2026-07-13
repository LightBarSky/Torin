using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace DTOs.DTO
{
    public class HeartBeatDTO
    {
        [JsonProperty("service_name")]
        public string ServiceName { get; set; } = string.Empty;
        [JsonProperty("timestamp")]
        public DateTime Timestamp { get; set; }
    }
}