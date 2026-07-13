using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class ParticipantChangedDTO
    {
        public long IdGroup { get; set; }
        public long ParticipantsCount { get; set; }
        public DateTime Date { get; set; }
    }
}