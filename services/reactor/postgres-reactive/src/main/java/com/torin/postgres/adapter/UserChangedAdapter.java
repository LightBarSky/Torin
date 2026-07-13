package com.torin.postgres.adapter;

import java.time.Instant;

import com.torin.postgres.entity.UserChanged;
import com.torin.postgres.port.UserChangedPort;
import com.torin.postgres.repository.UserChangedRepository;

import reactor.core.publisher.Flux;

public class UserChangedAdapter implements UserChangedPort {
    private final UserChangedRepository userChangedRepository;

    public UserChangedAdapter(UserChangedRepository userChangedRepository) {
        this.userChangedRepository = userChangedRepository;
    }
    @Override
    public Flux<UserChanged> findAllByIdUser(Long idUser) {
        return userChangedRepository.findAllByIdUser(idUser);
    }

    @Override
    public Flux<UserChanged> findAllByIdUserAndUpdatedAtBetween(Long idUser, Instant from, Instant to) {
        return userChangedRepository.findAllByIdUserAndUpdatedAtBetween(idUser, from, to);
    }
}
