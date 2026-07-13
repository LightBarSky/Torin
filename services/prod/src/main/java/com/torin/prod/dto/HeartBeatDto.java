package com.torin.prod.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeartBeatDto {
    @JsonProperty("service_name")
    private String serviceName;
    @JsonProperty("timestamp")
    private Instant timestamp;
}
