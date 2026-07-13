package com.torin.dbService.r2dbc.port;

import com.torin.dbService.r2dbc.entity.Handler;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface HandlerPort {

    Mono<Handler> findById(Long id);

    Mono<Handler> save(Handler handler);

    Mono<Void> deleteById(Long id);

    Flux<Handler> findAllByCategory(String category);

    Flux<Handler> findAllByOrderByIdAsc();

    Mono<Long> findIdByMinCountGroup(String category);

    Mono<Integer> updateCountGroupByIdIncrement(Long id);

    Mono<Integer> updateCountGroupByIdDecrement(Long id);
}
