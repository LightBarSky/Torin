package com.torin.core.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessagesPropertiesDto(
        Long id,

        @JsonProperty("id_message") Long idMessage,

        @JsonProperty("is_comments") String isComments,

        @JsonProperty("id_group") String idGroup,

        @JsonProperty("id_from") String idFrom,

        @JsonProperty("grouped_id") String groupedId,

        @JsonProperty("identity_id") String identityId,

        String flags,
        String flags2,

        @JsonProperty("has_text") Boolean hasText,

        @JsonProperty("has_media") Boolean hasMedia,

        @JsonProperty("media_type") String mediaType,

        @JsonProperty("media_value") Object mediaValue,

        Integer forwards,

        @JsonProperty("is_forward") Boolean isForward,

        @JsonProperty("fwd_value") Object fwdValue,

        Integer views,
        Integer replies,

        @JsonProperty("via_bot_id") String viaBotId,

        @JsonProperty("via_business_bot_id") String viaBusinessBotId,

        @JsonProperty("edit_date") Instant editDate,

        Instant date,

        @JsonProperty("created_at") Instant createdAt,

        @JsonProperty("updated_at") Instant updatedAt) {
}