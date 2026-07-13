package com.torin.dbService.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KafkaObjectDto(
        @JsonProperty("Mode")
        String mode,

        @JsonProperty("SerializeObjects")
        String serializeObjects
) {
}
