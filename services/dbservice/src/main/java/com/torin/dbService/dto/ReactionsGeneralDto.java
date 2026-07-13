package com.torin.dbService.dto;

import java.time.Instant;

public record ReactionsGeneralDto(
        Long idGroup,
        Long idMessage,
        Integer isComments,
        String reaction,
        Integer count,
        Instant date
) {
}
