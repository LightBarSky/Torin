package com.torin.postgres.service;

import java.time.Instant;

import com.torin.core.dto.UserChangedDto;
import com.torin.postgres.helper.Mapper;
import com.torin.postgres.port.UserChangedPort;

import reactor.core.publisher.Flux;

public class UserChangedService {
    
    private UserChangedPort userChangedPort;

    public UserChangedService(UserChangedPort userChangedPort) {
        this.userChangedPort = userChangedPort;
    }

    public Flux<UserChangedDto> findAll(Long idUser) {
        return userChangedPort.findAllByIdUser(idUser).map(Mapper::mapperToDto);
    }

    public Flux<UserChangedDto> findAllBetweenUpdatedAt(Long idUser, Instant from, Instant to) {
        return userChangedPort.findAllByIdUserAndUpdatedAtBetween(idUser, from, to).map(Mapper::mapperToDto);
    }
}
