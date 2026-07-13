package com.torin.core.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserChangedDto(
        Long id,

        @JsonProperty("id_user")
        Long idUser,

        @JsonProperty("first_name")
        String firstName,

        @JsonProperty("last_name")
        String lastName,

        String username,
        String number,

        @JsonProperty("user_photo")
        String userPhoto,

        @JsonProperty("updated_at")
        Instant updatedAt,

        String birthday,
        String flags,
        String flags2,

        @JsonProperty("flags_full")
        String flagsFull,

        @JsonProperty("flags2_full")
        String flags2Full,

        String about,

        @JsonProperty("bot_info")
        String botInfo,

        @JsonProperty("personal_channel_id")
        Long personalChannelId,

        @JsonProperty("location_address")
        String locationAddress,

        @JsonProperty("location_lat")
        Double locationLat,

        @JsonProperty("location_lon")
        Double locationLon,

        @JsonProperty("location_radius")
        Integer locationRadius
) {
}
