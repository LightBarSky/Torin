package com.torin.dbService.r2dbc.adapter;

import java.time.Instant;

import com.torin.dbService.r2dbc.entity.WordGroupAll;
import com.torin.dbService.r2dbc.port.WordGroupAllPort;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class NoOpWordGroupAllAdapter implements WordGroupAllPort {

    @Override
    public Mono<WordGroupAll> findByIdGroup(Long idGroup) {
        return Mono.empty();
    }

    @Override
    public Flux<WordGroupAll> findByIdGroupIn(Long[] ids) {
        return Flux.empty();
    }

    @Override
    public Flux<WordGroupAll> findBatchByHandlerId(Long handlersId, Long offsetId, Integer limit) {
        return Flux.empty();
    }

    @Override
    public Mono<WordGroupAll> findOneByHashGroupAndHandlersId(String hashGroup, Long handlersId) {
        return Mono.empty();
    }

    @Override
    public Mono<WordGroupAll> findOneByIdGroupAndHandlersId(Long idGroup, Long handlersId) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return Mono.empty();
    }

    @Override
    public Mono<WordGroupAll> findById(Long id) {
        return Mono.empty();
    }

    @Override
    public Mono<Integer> updateHandlersIdAndTotalSRAndTotalDP(Long handlersId, Long id, Integer totalSendRequest,
            Integer totalDetectPrivate) {
        return Mono.empty();
    }

    @Override
    public Mono<Integer> updateLastHandle(Instant lastHandle, Long id) {
        return Mono.empty();
    }

    @Override
    public Mono<WordGroupAll> save(WordGroupAll wordGroupAll) {
        return Mono.empty();
    }
    
}
