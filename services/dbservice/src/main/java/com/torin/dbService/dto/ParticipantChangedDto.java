package com.torin.dbService.dto;

import java.time.Instant;

public record ParticipantChangedDto(
        Long idGroup,
        Long participantsCount,
        Instant date
) {
}
