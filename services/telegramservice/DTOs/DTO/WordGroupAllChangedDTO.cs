using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class WordGroupAllChangedDTO
    {
        public long? Id { get; set; }

        public long? IdGroup { get; set; }

        public string? InfoGroup { get; set; }

        public string? TitleGroup { get; set; }

        public string? FindGroup { get; set; }

        public string? HashGroup { get; set; }

        public int? Type { get; set; }

        public DateTime? Date { get; set; } = DateTime.UtcNow;

        public long? LinkedId { get; set; }
        public string? Flags { get; set; }
        public string? Flags2 { get; set; }
    }
}