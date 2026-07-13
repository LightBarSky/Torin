package com.torin.dbService.dto;

import java.time.Instant;

public record ChatDto(
        Long idUser,
        Long idGroup,
        Instant dateJoined
) {
}
