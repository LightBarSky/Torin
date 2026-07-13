package com.torin.postgres.adapter;

import java.time.Instant;

import com.torin.postgres.entity.UserChanged;
import com.torin.postgres.port.UserChangedPort;

import reactor.core.publisher.Flux;

public class NoOpUserChangedAdapter implements UserChangedPort {

    @Override
    public Flux<UserChanged> findAllByIdUser(Long idUser) {
        return Flux.empty();
    }

    @Override
    public Flux<UserChanged> findAllByIdUserAndUpdatedAtBetween(Long idUser, Instant from, Instant to) {
        return Flux.empty();
    }
    
}
