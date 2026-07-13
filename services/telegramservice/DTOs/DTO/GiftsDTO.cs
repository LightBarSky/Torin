using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class GiftsDTO
    {
        public long? IdGroup { get; set; }
        public long? IdFrom { get; set; }
        public string? IdGift { get; set; }
        public string? Message { get; set; }
        public string? TitleGift { get; set; }
        public string? Flags { get; set; }
        public string? Flags2 { get; set; }
        public long? Stars { get; set; }
        public int? AvailabilityTotal { get; set; }
        public long? ConvertStars { get; set; }
        public DateTime? Date { get; set; }
    }
}