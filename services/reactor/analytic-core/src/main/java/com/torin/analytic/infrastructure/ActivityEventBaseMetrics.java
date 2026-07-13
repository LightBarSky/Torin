package com.torin.analytic.infrastructure;

import java.time.Instant;

public record ActivityEventBaseMetrics(
        TypeEvent typeEvent,
        Instant date,
        String userId,
        int messages,
        Long reactions,
        int gifts,
        Long views,
        int publicationsAdmin,
        int publicationsUser) {
}
