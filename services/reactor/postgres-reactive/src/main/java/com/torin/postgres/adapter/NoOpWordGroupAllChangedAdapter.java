package com.torin.postgres.adapter;

import java.time.Instant;

import com.torin.postgres.entity.WordGroupAllChanged;
import com.torin.postgres.port.WordGroupAllChangedPort;

import reactor.core.publisher.Flux;

public class NoOpWordGroupAllChangedAdapter implements WordGroupAllChangedPort {

    @Override
    public Flux<WordGroupAllChanged> findAllByIdGroup(Long idGroup) {
        return Flux.empty();
    }

    @Override
    public Flux<WordGroupAllChanged> findAllByIdGroupAndDateBetween(Long idGroup, Instant from, Instant to) {
        return Flux.empty();
    }
    
}
