package com.torin.postgres.repository;

import java.time.Instant;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.torin.postgres.entity.WordGroupAllChanged;

import reactor.core.publisher.Flux;

public interface WordGroupAllChangedRepository extends ReactiveCrudRepository<WordGroupAllChanged, Long> {

    Flux<WordGroupAllChanged> findAllByIdGroup(Long idGroup);
    Flux<WordGroupAllChanged> findAllByIdGroupAndDateBetween(Long idGroup, Instant from, Instant to);
}
