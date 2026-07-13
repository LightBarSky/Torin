package com.torin.postgres.adapter;

import com.torin.postgres.entity.WordGroupAll;
import com.torin.postgres.port.WordGroupAllPort;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class NoOpWordGroupAllAdapter implements WordGroupAllPort {

    @Override
    public Mono<WordGroupAll> findByIdGroup(Long idGroup) {
        return Mono.empty();
    }

    @Override
    public Flux<WordGroupAll> findByFindGroup(String findGroup) {
        return Flux.empty();
    }

    @Override
    public Flux<WordGroupAll> findByIdGroupIn(Long[] ids) {
        return Flux.empty();
    }
    
}
