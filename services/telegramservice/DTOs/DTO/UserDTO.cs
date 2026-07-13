using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class UserDTO
    {
        public long IdUser { get; set; }

        public string? FirstName { get; set; }

        public string? LastName { get; set; }

        public string? Username { get; set; }

        public string? Number { get; set; }

        public string? UserPhoto { get; set; }

        public string? Tags { get; set; }

        public bool IsGeo { get; set; }

        public DateTime UpdatedAt { get; set; }

        public string? Birthday { get; set; }

        public string? Flags { get; set; }

        public string? Flags2 { get; set; }

        public string? FlagsFull { get; set; }

        public string? Flags2Full { get; set; }

        public string? About { get; set; }

        public bool IsBot { get; set; }
        public string? BotInfo { get; set; }
        public long? PersonalChannelId { get; set; }
        public string? LocationAddress { get; set; }
        public double? LocationLat { get; set; }
        public double? LocationLon { get; set; }
        public int? LocationRadius { get; set; }
    }
}