package com.torin.core.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParticipantChangedDto(Long id, @JsonProperty("participants_count") Long participantsCount,
        @JsonProperty("id_group") Long idGroup, Instant date) {
}
