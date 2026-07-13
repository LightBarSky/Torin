using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class WordGroupAllDTO
    {
        public long? Id { get; set; }

        public long? IdGroup { get; set; }

        public string? InfoGroup { get; set; }

        public string? TitleGroup { get; set; }

        public string? FindGroup { get; set; }

        public string? HashGroup { get; set; }

        public long? IdUserJoin { get; set; }

        public int? Type { get; set; }

        public long? HandlersId { get; set; }

        public DateTime? LastUpdate { get; set; }

        public DateTime? LastHandle { get; set; }

        public int? TotalSendRequest { get; set; } = 0;

        public int? TotalDetectPrivate { get; set; } = 0;

        public long? LinkedId { get; set; }
        public long? ParticipantsCount { get; set; }
        public DateTime? CreatedDate { get; set; }
        public string? Flags { get; set; }
        public string? Flags2 { get; set; }
    }
}