using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace TelegramService.Infrastructure
{
    public static class TelegramHelper
    {
        public static string SerialezeObjects<T>(List<T> objects)
        {
            var options = new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
                Converters = { new JsonStringEnumConverter() },
                WriteIndented = false
            };
            return System.Text.Json.JsonSerializer.Serialize(objects, options);
        }
    }
}