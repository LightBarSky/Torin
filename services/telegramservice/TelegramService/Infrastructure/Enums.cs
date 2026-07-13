namespace TelegramService.Infrastructure
{
    public enum ModesForDB
    {
        UPDATE,
        DELETE,
        INSERT,
        INSERT_IGNORE
    }

    public enum ReactionType
    {
        REACTION_PAID
    }

    public enum MediaType
    {
        PHOTO,
        GEO,
        CONTACT,
        UNSUPPORTED_MEDIA,
        VIDEO,
        ROUND_VIDEO,
        VOICE, DOCUMENT,
        WEB_PAGE,
        VENUE,
        GAME,
        INVOICE,
        GEO_LIVE,
        POLL,
        DICE,
        STORY,
        GIVEAWAY,
        GIVEAWAY_RESULTS,
        PAID_MEDIA
    }

}