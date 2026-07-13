package com.torin.dbService.dto;

import java.time.Instant;

public record ReactionDto(
        Long idGroup,
        Long idMessage,
        Long idUser,
        String reaction,
        Instant date
) {
}
