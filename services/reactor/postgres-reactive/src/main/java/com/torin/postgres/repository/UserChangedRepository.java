package com.torin.postgres.repository;

import java.time.Instant;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.torin.postgres.entity.UserChanged;

import reactor.core.publisher.Flux;

public interface UserChangedRepository extends ReactiveCrudRepository<UserChanged, Long> {

    Flux<UserChanged> findAllByIdUser(Long idUser);
    Flux<UserChanged> findAllByIdUserAndUpdatedAtBetween(Long idUser, Instant from, Instant to);
}
