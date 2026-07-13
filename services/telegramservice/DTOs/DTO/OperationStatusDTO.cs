using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection.Metadata;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class OperationStatusDTO
    {
        public bool Status { get; set; }
        public string? Message { get; set; }
    }
}