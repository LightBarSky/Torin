package com.torin.core.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDto(
                Long id,

                @JsonProperty("id_user") Long idUser,

                @JsonProperty("first_name") String firstName,

                @JsonProperty("last_name") String lastName,

                String username,
                String number,

                @JsonProperty("pg_tags") String pgTags,

                @JsonProperty("is_geo") Boolean isGeo,

                @JsonProperty("updated_at") Instant updatedAt,

                String birthday,
                String flags,
                String flags2,

                @JsonProperty("flags_full") String flagsFull,

                @JsonProperty("flags2_full") String flags2Full,

                String about,

                @JsonProperty("is_bot") Boolean isBot,

                @JsonProperty("bot_info") String botInfo,

                @JsonProperty("personal_channel_id") Long personalChannelId,

                @JsonProperty("location_address") String locationAddress,

                GeoPointDto location,

                @JsonProperty("location_radius") Integer locationRadius,

                Long version) {
}
