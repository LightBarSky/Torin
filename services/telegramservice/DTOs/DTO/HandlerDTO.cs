using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DTOs.DTO
{
    public class HandlerDTO
    {
        public long Id { get; set; }

        public long? ApiId { get; set; }

        public string? Hash { get; set; }

        public string? Phone { get; set; }

        public string? DirectoryForUserPhoto { get; set; }

        public string? DirectoryForMedia { get; set; }

        public string? Category { get; set; }

        public int? CountGroup { get; set; }

        public string? NameHandler { get; set; }
        public string? Status { get; set; }
    }
}