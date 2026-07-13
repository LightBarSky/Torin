package com.torin.postgres.port;

import java.time.Instant;

import com.torin.postgres.entity.WordGroupAllChanged;

import reactor.core.publisher.Flux;

public interface WordGroupAllChangedPort {
    Flux<WordGroupAllChanged> findAllByIdGroup(Long idGroup);
    Flux<WordGroupAllChanged> findAllByIdGroupAndDateBetween(Long idGroup, Instant from, Instant to);
}
