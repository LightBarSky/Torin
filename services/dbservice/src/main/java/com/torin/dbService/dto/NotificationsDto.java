package com.torin.dbService.dto;

import java.time.Instant;

public record NotificationsDto(
        Long id,
        Instant timestamp,
        String type,
        String message,
        Boolean read,
        String formattedDate
) {
}
