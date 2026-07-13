package com.torin.analytic.infrastructure;

import java.time.Instant;

public record ActivityEventAQandAL(TypeEvent typeEvent, String idFrom, Instant date) {
}
