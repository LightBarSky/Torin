using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace TelegramService.Infrastructure
{
    public class SavedState
    {
        public ConcurrentDictionary<long, long> Channels { get; set; }
        public ConcurrentDictionary<long, long> Users { get; set; }

        public SavedState()
        {
            Channels = new();
            Users = new();
        }

        public void Clear()
        {
            Channels.Clear();
            Users.Clear();
        }
    }
}