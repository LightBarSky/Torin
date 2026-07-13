using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class StatusDTO
    {
        public string? RunningHandlers { get; set; } = null;
        public string? AllHandlers { get; set; } = null;
        public string? ParseGroupHandlers { get; set; } = null;
        public string? ControlUserHandlers { get; set; } = null;
        public string? StatusDB { get; set; } = null;
        public string? StatusKafka { get; set; } = null;
    }
}