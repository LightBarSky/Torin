using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Reflection.Metadata;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class TaskChatDTO
    {
        public long? Id { get; set; } = null;

        public long IdChat { get; set; }

        public long? OffsetIdNewMessage { get; set; }

        public long? OffsetIdOldMessage { get; set; }

        public DateTime? DateParseUser { get; set; }

        public DateTime? DateOfLastRecord { get; set; }
    }
}