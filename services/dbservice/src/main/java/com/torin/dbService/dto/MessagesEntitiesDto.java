package com.torin.dbService.dto;

import java.time.Instant;

public record MessagesEntitiesDto(
        Long idGroup,
        Long isComments,
        Long idMessage,
        String type,
        Integer entityOffset,
        Integer length,
        String value,
        Instant date) {
}
