package com.torin.dbService.r2dbc.adapter;

import com.torin.dbService.r2dbc.entity.Handler;
import com.torin.dbService.r2dbc.port.HandlerPort;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class NoOpHandlerAdapter implements HandlerPort {

    @Override
    public Flux<Handler> findAllByCategory(String category) {
        return Flux.empty();
    }

    @Override
    public Flux<Handler> findAllByOrderByIdAsc() {
        return Flux.empty();
    }

    @Override
    public Mono<Long> findIdByMinCountGroup(String category) {
        return Mono.empty();
    }

    @Override
    public Mono<Integer> updateCountGroupByIdIncrement(Long id) {
        return Mono.empty();
    }

    @Override
    public Mono<Integer> updateCountGroupByIdDecrement(Long id) {
        return Mono.empty();
    }

    @Override
    public Mono<Handler> findById(Long id) {
        return Mono.empty();
    }

    @Override
    public Mono<Handler> save(Handler handler) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return Mono.empty();
    }
    
}
