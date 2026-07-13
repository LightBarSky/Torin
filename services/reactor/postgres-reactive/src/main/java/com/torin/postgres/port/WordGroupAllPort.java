package com.torin.postgres.port;

import com.torin.postgres.entity.WordGroupAll;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WordGroupAllPort {
    Mono<WordGroupAll> findByIdGroup(Long idGroup);

    Flux<WordGroupAll> findByFindGroup(String findGroup);

    Flux<WordGroupAll> findByIdGroupIn(Long[] ids);
}
