using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class NotificationsDTO
    {
        public long? Id { get; set; }
        public DateTime? Timestamp { get; set; }
        public string? Type { get; set; }
        public string? Message { get; set; }
        public bool? read { get; set; }
    }
}