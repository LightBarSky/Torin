using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class MessagesPropertiesDTO
    {
        public long? IdGroup { get; set; }
        public long? IdMessage { get; set; }
        public int? IsComments { get; set; }
        public long? IdFrom { get; set; }
        public string? Flags { get; set; }
        public string? Flags2 { get; set; }
        public long? Forwards { get; set; }
        public long? GroupedId { get; set; }
        public bool? HasMedia { get; set; }
        public string? MediaType { get; set; }
        public string? MediaValue { get; set; }
        public bool? HasText { get; set; }
        public bool? IsForwards { get; set; }
        public string? FwdValue { get; set; }
        public long? Views { get; set; }
        public long? Replies { get; set; }
        public long? ViaBotId { get; set; }
        public long? ViaBusinessBotId { get; set; }
        public DateTime? EditDate { get; set; }
        public DateTime? Date { get; set; }
    }
}