package com.torin.core.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GiftsDto(Long id, @JsonProperty("id_group") String idGroup, @JsonProperty("id_from") String idFrom,
        Instant date) {
}
