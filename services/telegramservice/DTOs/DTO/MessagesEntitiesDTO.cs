using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class MessagesEntitiesDTO
    {
        public long? IdGroup { get; set; }
        public long? IdMessage { get; set; }
        public int? IsComments { get; set; }
        public string? Type { get; set; }
        public int? EntityOffset { get; set; }
        public int? Length { get; set; }
        public string? Value { get; set; }
        public DateTime? Date { get; set; }
    }
}