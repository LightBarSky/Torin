using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace DTOs.DTO
{
    public class LogEntryDTO
    {
        [JsonProperty("handler_id")]
        public string HandlerId { get; set; } = string.Empty;
        [JsonProperty("timestamp")]
        public DateTime Timestamp { get; set; }
        [JsonProperty("formatter_timestamp")]
        public string? FormatterTimestamp { get; set; }
        [JsonProperty("message")]
        public string Message { get; set; } = string.Empty;
        [JsonProperty("level")]
        public string Level { get; set; } = string.Empty;
    }
}