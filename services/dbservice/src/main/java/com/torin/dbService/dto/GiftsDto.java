package com.torin.dbService.dto;

import java.time.Instant;

public record GiftsDto(
        Long idGroup,
        Long idFrom,
        String idGift,
        String message,
        String titleGift,
        String flags,
        String flags2,
        Long stars,
        Long convertStars,
        Integer availabilityTotal,
        Instant date
) {
}
