package com.torin.dbService.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeartBeatDto(
        @JsonProperty("service_name")
        String serviceName,

        @JsonProperty("timestamp")
        Instant timestamp
) {
}
