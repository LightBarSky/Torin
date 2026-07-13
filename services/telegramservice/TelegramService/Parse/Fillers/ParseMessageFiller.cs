using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using DTOs.DTO;
using Newtonsoft.Json;
using TelegramService.Infrastructure;
using TL;

namespace TelegramService.Parse.Fillers
{
    public static class ParseMessageFiller
    {
        public static void FillerEntities(Message msg, long chat_id, ref ParseMessageStorage storage, int is_comments)
        {
            if (msg.entities is not null)
            {
                foreach (var entity in msg.entities)
                {
                    switch (entity)
                    {
                        case MessageEntityBankCard or
                            MessageEntityCashtag or
                            MessageEntityEmail or
                            MessageEntityHashtag or
                            MessageEntityPhone or
                            MessageEntityUrl or
                            MessageEntityUnknown:
                            storage.sendMessageEntities.Add(new MessagesEntitiesDTO()
                            {
                                IdGroup = chat_id,
                                IdMessage = msg.ID,
                                IsComments = is_comments,
                                Type = entity.Type,
                                EntityOffset = entity.offset,
                                Length = entity.Length,
                                Value = msg.message.Substring(entity.offset, entity.length),
                                Date = msg.Date
                            });
                            break;
                        case MessageEntityTextUrl messageEntityTextUrl:
                            storage.sendMessageEntities.Add(new MessagesEntitiesDTO()
                            {
                                IdGroup = chat_id,
                                IdMessage = msg.ID,
                                IsComments = is_comments,
                                Type = entity.Type,
                                EntityOffset = entity.offset,
                                Length = entity.Length,
                                Value = messageEntityTextUrl.url,
                                Date = msg.Date
                            });
                            break;
                    }
                }
            }
        }

        public static void FillerReactionsGeneral(Message msg, long chat_id, ref ParseMessageStorage storage, int is_comments)
        {
            if (msg.Reactions?.results is not null)
            {
                foreach (var reaction in msg.reactions.results)
                {
                    string? emoji = null;
                    if (reaction.reaction is TL.ReactionEmoji emoji1)
                    {
                        emoji = emoji1.emoticon;
                    }
                    else if (reaction.reaction is TL.ReactionCustomEmoji emojiCust)
                    {
                        emoji = emojiCust.document_id.ToString();
                    }
                    else if (reaction.reaction is TL.ReactionPaid)
                    {
                        emoji = ReactionType.REACTION_PAID.ToString();
                    }

                    storage.sendReactionsGeneral.Add(new ReactionGeneralDTO()
                    {
                        IdGroup = chat_id,
                        IdMessage = msg.ID,
                        IsComments = is_comments,
                        Count = reaction.count,
                        Reaction = emoji,
                        Date = msg.Date
                    });
                }
            }
        }

        public static void FillerReactions(Message msg, long chat_id, ref ParseMessageStorage storage)
        {
            if (msg.Reactions?.recent_reactions is not null)
            {
                foreach (var item in msg.Reactions.recent_reactions)
                {
                    string? emoji = null;
                    if (item.reaction is TL.ReactionEmoji emoji1)
                    {
                        emoji = emoji1.emoticon;
                    }
                    else if (item.reaction is TL.ReactionCustomEmoji emojiCust)
                    {
                        emoji = emojiCust.document_id.ToString();
                    }
                    else if (item.reaction is TL.ReactionPaid)
                    {
                        emoji = ReactionType.REACTION_PAID.ToString();
                    }

                    storage.sendReactions.Add(new ReactionDTO()
                    {
                        IdGroup = chat_id,
                        IdMessage = msg.ID,
                        IdUser = item.peer_id?.ID,
                        Reaction = emoji,
                        Date = item.date
                    });
                }
            }
        }

        public static MessagesPropertiesDTO FillerProperties(Message msg, long chat_id, int is_comments)
        {
            MessagesPropertiesDTO messagePropertiesSingle = new MessagesPropertiesDTO()
            {
                IdGroup = chat_id,
                IdMessage = msg.ID,
                IdFrom = msg.From?.ID,
                IsComments = is_comments,
                Flags = msg.flags.ToString(),
                Flags2 = msg.flags2.ToString(),
                GroupedId = (msg.grouped_id == 0) ? null : msg.grouped_id,
                HasMedia = msg.flags.HasFlag(Message.Flags.has_media),
                HasText = !string.IsNullOrEmpty(msg.message),
                Forwards = msg.forwards,
                Views = msg.views,
                Replies = msg.replies?.replies,
                ViaBotId = msg.via_bot_id,
                ViaBusinessBotId = msg.via_business_bot_id,
                EditDate = msg.flags.HasFlag(Message.Flags.has_edit_date) ? msg.edit_date : null,
                Date = msg.Date
            };

            if (msg.flags.HasFlag(Message.Flags.has_fwd_from))
            {
                messagePropertiesSingle.IsForwards = true;
                messagePropertiesSingle.FwdValue = JsonConvert.SerializeObject(new
                {
                    channel_post = msg.fwd_from?.channel_post,
                    date = msg.fwd_from?.date,
                    flags = msg.fwd_from?.flags.ToString(),
                    from_id = msg.fwd_from?.from_id,
                    from_name = msg.fwd_from?.from_name,
                    post_author = msg.fwd_from?.post_author,
                    psa_type = msg.fwd_from?.psa_type,
                    saved_data = msg.fwd_from?.saved_date,
                    saved_from_id = msg.fwd_from?.saved_from_id,
                    saved_from_msg_id = msg.fwd_from?.saved_from_msg_id,
                    saved_from_name = msg.fwd_from?.saved_from_name,
                    saved_from_peer_id = msg.fwd_from?.saved_from_peer?.ID
                });
            }

            if (msg.flags.HasFlag(Message.Flags.has_media))
            {
                switch (msg.media)
                {
                    case MessageMediaPhoto photo:
                        messagePropertiesSingle.MediaType = MediaType.PHOTO.ToString();
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new { flags = photo.flags.ToString() }
                            );
                        break;
                    case MessageMediaGeo geo:
                        messagePropertiesSingle.MediaType = MediaType.GEO.ToString();
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new
                            {
                                lat = geo.geo?.lat,
                                lon = geo.geo?.lon,
                                accuracy_radius = geo.geo?.accuracy_radius,
                                flags_geo = geo.geo?.flags.ToString()
                            });
                        break;
                    case MessageMediaContact contact:
                        messagePropertiesSingle.MediaType = MediaType.CONTACT.ToString();
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new
                            {
                                phone_number = contact.phone_number,
                                first_name = contact.first_name,
                                last_name = contact.last_name,
                                vcard = contact.vcard,
                                user_id = contact.user_id
                            });
                        break;
                    case MessageMediaUnsupported unsupported:
                        messagePropertiesSingle.MediaType = MediaType.UNSUPPORTED_MEDIA.ToString();
                        break;
                    case MessageMediaDocument document:
                        messagePropertiesSingle.MediaType = document switch
                        {
                            _ when document.flags.HasFlag(MessageMediaDocument.Flags.video) => MediaType.VIDEO.ToString(),
                            _ when document.flags.HasFlag(MessageMediaDocument.Flags.round) => MediaType.ROUND_VIDEO.ToString(),
                            _ when document.flags.HasFlag(MessageMediaDocument.Flags.voice) => MediaType.VOICE.ToString(),
                            _ => MediaType.DOCUMENT.ToString()
                        };
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new
                            {
                                flags = document.flags.ToString()
                            });
                        break;
                    case MessageMediaWebPage webPage:
                        messagePropertiesSingle.MediaType = MediaType.WEB_PAGE.ToString();
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new
                            {
                                flags = webPage.flags.ToString(),
                                url = webPage.webpage?.Url,
                                id = webPage.webpage?.ID
                            });
                        break;
                    case MessageMediaVenue venue:
                        messagePropertiesSingle.MediaType = MediaType.VENUE.ToString();
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new
                            {
                                title = venue.title,
                                address = venue.address,
                                provider = venue.provider,
                                venue_id = venue.venue_id,
                                venue_type = venue.venue_type,
                                lat = venue.geo?.lat,
                                lon = venue.geo?.lon,
                                accuracy_radius = venue.geo?.accuracy_radius,
                                flags_geo = venue.geo?.flags.ToString()
                            });
                        break;
                    case MessageMediaGame game:
                        messagePropertiesSingle.MediaType = MediaType.GAME.ToString();
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new
                            {
                                title = game.game?.title,
                                description = game.game?.description,
                                flags_game = game.game?.flags.ToString(),
                                id = game.game?.id,
                                short_name = game.game?.short_name
                            });
                        break;
                    case MessageMediaInvoice invoice:
                        messagePropertiesSingle.MediaType = MediaType.INVOICE.ToString();
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new
                            {
                                currency = invoice.currency,
                                description = invoice.description,
                                title = invoice.title,
                                flags = invoice.flags.ToString(),
                                total_amount = invoice.total_amount
                            });
                        break;
                    case MessageMediaGeoLive geoLive:
                        messagePropertiesSingle.MediaType = MediaType.GEO_LIVE.ToString();
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new
                            {
                                lat = geoLive.geo?.lat,
                                lon = geoLive.geo?.lon,
                                accuracy_radius = geoLive.geo?.accuracy_radius,
                                flags_geo = geoLive.geo?.flags.ToString(),
                                flags = geoLive.flags.ToString(),
                                heading = geoLive.heading,
                                period = geoLive.period,
                                proximity_notification_radius = geoLive.proximity_notification_radius
                            });
                        break;
                    case MessageMediaPoll poll:
                        messagePropertiesSingle.MediaType = MediaType.POLL.ToString();
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new
                            {
                                question = poll.poll?.question,
                                answers = poll.poll?.answers?.Select(x => new
                                {
                                    option = x.option,
                                    text = x.text
                                }).ToArray(),
                                close_date = poll.poll?.close_date,
                                close_period = poll.poll?.close_period,
                                flags_poll = poll.poll?.flags.ToString(),
                                id_poll = poll.poll?.id,
                                flags_results = poll.results?.flags.ToString(),
                                recent_voters = poll.results?.recent_voters?.Select(x => new
                                {
                                    ID = x.ID
                                }).ToArray(),
                                results = poll.results?.results?.Select(x => new
                                {
                                    option = x.option,
                                    voters = x.voters,
                                    flags = x.flags.ToString()
                                }).ToArray(),
                                solution = poll.results?.solution,
                                solution_entities = poll.results?.solution_entities?.Select(x => new
                                {
                                    length = x.length,
                                    offset = x.offset,
                                    type = x.Type
                                }).ToArray(),
                                total_voters = poll.results?.total_voters
                            });
                        break;
                    case MessageMediaDice dice:
                        messagePropertiesSingle.MediaType = MediaType.DICE.ToString();
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new
                            {
                                emoticon = dice.emoticon,
                                value = dice.value
                            });
                        break;
                    case MessageMediaStory story:
                        messagePropertiesSingle.MediaType = MediaType.STORY.ToString();
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new
                            {
                                peer_id = story.peer?.ID,
                                flags = story.flags.ToString(),
                                id = story.id,
                                story_id = story.story?.ID
                            });
                        break;
                    case MessageMediaGiveaway giveAway:
                        messagePropertiesSingle.MediaType = MediaType.GIVEAWAY.ToString();
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new
                            {
                                channels = giveAway.channels,
                                countries_iso2 = giveAway.countries_iso2,
                                flags = giveAway.flags.ToString(),
                                months = giveAway.months,
                                prize_description = giveAway.prize_description,
                                quantity = giveAway.quantity,
                                stars = giveAway.stars,
                                until_date = giveAway.until_date
                            });
                        break;
                    case MessageMediaGiveawayResults giveAwayResults:
                        messagePropertiesSingle.MediaType = MediaType.GIVEAWAY_RESULTS.ToString();
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new
                            {
                                additional_peers_count = giveAwayResults.additional_peers_count,
                                channel_id = giveAwayResults.channel_id,
                                flags = giveAwayResults.flags.ToString(),
                                months = giveAwayResults.months,
                                prize_description = giveAwayResults.prize_description,
                                launch_msg_id = giveAwayResults.launch_msg_id,
                                stars = giveAwayResults.stars,
                                until_date = giveAwayResults.until_date,
                                unclaimed_count = giveAwayResults.unclaimed_count,
                                winners = giveAwayResults.winners,
                                winners_count = giveAwayResults.winners_count
                            });
                        break;
                    case MessageMediaPaidMedia paidMedia:
                        messagePropertiesSingle.MediaType = MediaType.PAID_MEDIA.ToString();
                        messagePropertiesSingle.MediaValue = JsonConvert.SerializeObject(
                            new
                            {
                                stars_amount = paidMedia.stars_amount
                            });
                        break;
                }
            }
            return messagePropertiesSingle;
        }
    }
}