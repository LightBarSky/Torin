package com.torin.core.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReactionsDto(Long id, @JsonProperty("id_message") Long idMessage, String reaction,
        @JsonProperty("id_group") String idGroup, @JsonProperty("id_user") String idUser, Instant date) {
}
