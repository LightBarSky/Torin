package com.torin.dbService.dto;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

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

    @JsonProperty("message")
    public String message;
    @JsonProperty("level")
    public String level;

    @JsonProperty("formatter_timestamp")
    public String getFormatterTimestamp() {
        if (timestamp == null) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return timestamp
                .atZone(ZoneOffset.UTC)
                .format(formatter);
    }

    public static LogEntryDto create(String handlerId, String message, String level, Instant timestamp) {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        return new LogEntryDto(handlerId, timestamp, message, level);
    }
}
