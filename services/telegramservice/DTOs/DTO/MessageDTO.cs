using System;

namespace DTOs.DTO;

public class MessageDTO
{
    public long? IdGroup { get; set; }
    public long? IsComments { get; set; }
    public long? ReplyToPost { get; set; }
    public long? IdMessage { get; set; }
    public long? IdUser { get; set; }
    public long? IdGroupedMessage { get; set; }
    public string? ContentText { get; set; }
    public string? ContentMedia { get; set; }
    public long? IdReply { get; set; }
    public DateTime? Date { get; set; }
}