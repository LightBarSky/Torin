package com.torin.analytic.infrastructure;

import java.time.LocalDate;

public record ActivityEventBaseUser(
        Long groupId,
        LocalDate date,
        int messages,
        int reactions,
        int gifts) {
}
