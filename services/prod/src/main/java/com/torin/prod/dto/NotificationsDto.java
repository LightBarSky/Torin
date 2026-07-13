package com.torin.prod.dto;

import java.time.Instant;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationsDto {
    private Long id;
    private Instant timestamp;
    private String type;
    private String message;
    private Boolean read;
    private String formattedDate;

    public NotificationsDto(Instant timestamp, String type, String message) {
        this.timestamp = timestamp;
        this.type = type;
        this.message = message;
        this.read = false;
    }
}
