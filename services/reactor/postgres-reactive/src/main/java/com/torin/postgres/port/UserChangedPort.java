package com.torin.postgres.port;

import java.time.Instant;

import com.torin.postgres.entity.UserChanged;

import reactor.core.publisher.Flux;

public interface UserChangedPort {
    Flux<UserChanged> findAllByIdUser(Long idUser);
    Flux<UserChanged> findAllByIdUserAndUpdatedAtBetween(Long idUser, Instant from, Instant to);
}
