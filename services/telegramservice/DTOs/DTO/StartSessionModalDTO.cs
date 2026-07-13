using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class StartSessionModalDTO
    {
        public required string ApiId { get; set; }
        public required string Hash { get; set; }
        public required string PhoneNumber { get; set; }
        public string? WithQR { get; set; }
    }
}