using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace TelegramService.Setting
{
    public class ParseGroupOptions
    {
        [Required]
        public int? DaysDelayMessage { get; init; }
        [Required]
        public int? BatchForKafka { get; init; }
        [Required]
        public int? BatchForUser { get; init; }
        [Required]
        public int? TotalDetectPrivate { get; init; }
        [Required]
        public int? TotalSendRequest { get; init; }
        [Required]
        public int? DelayResolveUsernameS { get; init; }
        [Required]
        public int? DelayGetFullChatS { get; init; }
        [Required]
        public int? DelayLoaderGroupS { get; init; }
        [Required]
        public int? DelayUpdateGroupH { get; init; }
        [Required]
        public int? DelayParseGiftsMS { get; init; }
        [Required]
        public int? DelayParseMessageMS { get; init; }
        [Required]
        public int? DelayParseRepliesMS { get; init; }
        [Required]
        public int? DaysDelayParseUser { get; init; }
        [Required]
        public int? DelayJoinChannelS { get; init; }
        [Required]
        public int? DelayRestartM { get; init; }
        [Required]
        public int? CountMessagesParseOnChat { get; init; }
        [Required]
        public bool? ParseMessage { get; init; }
        [Required]
        public int? ParseMessageRubikonDays { get; init; }
        [Required]
        public bool? ParseMessageOnMessages { get; init; }
        [Required]
        public bool? ParseMessageOnProperties { get; init; }
        [Required]
        public bool? ParseMessageOnEntities { get; init; }
        [Required]
        public bool? ParseMessageOnReactions { get; init; }
        [Required]
        public bool? ParseMessageOnReactionsGeneral { get; init; }
        [Required]
        public bool? ParseMessageOnUsers { get; init; }
        [Required]
        public bool? ParseUser { get; init; }
        [Required]
        public bool? ParseGifts { get; init; }
    }
}