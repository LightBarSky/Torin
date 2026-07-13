package com.torin.postgres.service;

import java.time.Instant;

import com.torin.core.dto.WordGroupAllChangedDto;
import com.torin.postgres.helper.Mapper;
import com.torin.postgres.port.WordGroupAllChangedPort;

import reactor.core.publisher.Flux;

public class WordGroupAllChangedService {
    
    private WordGroupAllChangedPort wordGroupAllChangedPort;

    public WordGroupAllChangedService(WordGroupAllChangedPort wordGroupAllChangedPort) {
        this.wordGroupAllChangedPort = wordGroupAllChangedPort;
    }

    public Flux<WordGroupAllChangedDto> findAll(Long idGroup) {
        return wordGroupAllChangedPort.findAllByIdGroup(idGroup).map(Mapper::mapperToDto);
    }

    public Flux<WordGroupAllChangedDto> findAllBetweenDate(Long idGroup, Instant from, Instant to) {
        return wordGroupAllChangedPort.findAllByIdGroupAndDateBetween(idGroup, from, to).map(Mapper::mapperToDto);
    }
}
