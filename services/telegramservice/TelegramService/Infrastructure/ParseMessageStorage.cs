using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using DTOs.DTO;

namespace TelegramService.Infrastructure
{
    public class ParseMessageStorage
    {
        public List<UserAndFullUser> sendParticipant { get; set; }

        public List<ReactionDTO> sendReactions { get; set; }
        public List<ReactionGeneralDTO> sendReactionsGeneral { get; set; }
        public List<MessageDTO> sendMessage { get; set; }
        public List<MessagesEntitiesDTO> sendMessageEntities { get; set; }
        public List<MessagesPropertiesDTO> sendMessagesProperties { get; set; }

        public ParseMessageStorage()
        {
            this.sendParticipant = new();
            this.sendReactions = new();
            this.sendReactionsGeneral = new();
            this.sendMessage = new();
            this.sendMessagesProperties = new();
            this.sendMessageEntities = new();
        }

        public ParseMessageStorage(int capacityMessage) : this()
        {
            sendMessage.EnsureCapacity(capacityMessage);
        }
        
        public void ClearStorage()
        {
            this.sendMessage.Clear();
            this.sendMessageEntities.Clear();
            this.sendMessagesProperties.Clear();
            this.sendParticipant.Clear();
            this.sendReactions.Clear();
            this.sendReactionsGeneral.Clear();
        }
    }
}