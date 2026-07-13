package com.torin.core.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReactionsGeneralDto(
                Long id,

                @JsonProperty("id_message") Long idMessage,

                String reaction,

                Long count,

                @JsonProperty("id_group") String idGroup,

                @JsonProperty("is_comments") String isComments,

                @JsonProperty("created_at") Instant createdAt,

                @JsonProperty("updated_at") Instant updatedAt,

                Instant date) {
}
