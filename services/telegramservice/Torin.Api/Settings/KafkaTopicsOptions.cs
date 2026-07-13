using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Torin.Api.Settings
{
    public class KafkaTopicsOptions
    {
        public Dictionary<string, string> Topics { get; set; } = new();
    }
}