package com.torin.prod.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogEntryDto {
    @JsonProperty("handler_id")
    private String handlerId;
    @JsonProperty("timestamp")
    private Instant timestamp;
    @JsonProperty("formatter_timestamp")
    public String formatterTimestamp;
    @JsonProperty("message")
    public String message;
    @JsonProperty("level")
    public String level;
    public Integer mode;

    public LogEntryDto(String handlerId, Integer mode) {
        this.handlerId = handlerId;
        this.mode = mode;
    }
}
