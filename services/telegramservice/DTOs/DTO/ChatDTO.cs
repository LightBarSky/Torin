using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class ChatDTO
    {
        public long? Id { get; set; }

        public long IdUser { get; set; }

        public long IdGroup { get; set; }
        public DateTime? DateJoined { get; set; }
    }
}