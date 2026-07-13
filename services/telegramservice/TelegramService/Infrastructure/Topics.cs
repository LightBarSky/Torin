using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace TelegramService.Infrastructure
{
    public enum TopicResolve
    {
        Messages,
        Messages_Properties,
        Messages_Entities,
        User,
        Chat,
        Admin_Chats,
        Gifts,
        Reactions,
        Reactions_General,
        Task_Chats,
        Word_Group_All
    }
    public class TopicsKafka
    {
        private readonly Dictionary<string, string> _topics;

        public TopicsKafka(Dictionary<string, string> topics)
        {
            _topics = topics;
        }

        public string GetTopic(TopicResolve type)
        {
            var key = type.ToString().ToLower();

            return _topics.TryGetValue(key, out var topic)
                ? topic
                : throw new KeyNotFoundException($"Topic '{key}' not found");
        }

        public void TryAnyTopic()
        {
            foreach (var item in Enum.GetNames(typeof(TopicResolve)))
            {
                string name = item.ToLower();
                string res = _topics.TryGetValue(name, out var val) ?
                (string.IsNullOrEmpty(val) ? throw new Exception($"Key '{item}' is null or empty") : val) :
                throw new KeyNotFoundException($"Topic '{item}' not found");
            }
        }
    }
}