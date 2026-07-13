using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class ReactionGeneralDTO
    {
        public long? IdGroup { get; set; }
        public long? IdMessage { get; set; }
        public int? IsComments { get; set; }
        public long? Count { get; set; }
        public string? Reaction { get; set; }
        public DateTime? Date { get; set; }
    }
}