package com.torin.dbService.dto;

import java.time.Instant;

public record MessagesPropertiesDto(
        Long idGroup,
        Long isComments,
        Long idMessage,
        Long idFrom,
        Long groupedId,
        String flags,
        String flags2,
        Boolean hasText,
        Boolean hasMedia,
        String mediaType,
        String mediaValue,
        Integer forwards,
        Boolean isForward,
        String fwdValue,
        Integer views,
        Integer replies,
        Long viaBotId,
        Long viaBusinessBotId,
        Instant editDate,
        Instant date) {
}
