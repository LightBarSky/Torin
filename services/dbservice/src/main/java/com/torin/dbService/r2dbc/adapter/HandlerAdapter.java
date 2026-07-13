package com.torin.dbService.r2dbc.adapter;

import com.torin.dbService.r2dbc.entity.Handler;
import com.torin.dbService.r2dbc.port.HandlerPort;
import com.torin.dbService.r2dbc.repository.HandlerRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class HandlerAdapter implements HandlerPort {

    private final HandlerRepository handlerRepository;

    public HandlerAdapter(HandlerRepository handlerRepository) {
        this.handlerRepository = handlerRepository;
    }

    @Override
    public Flux<Handler> findAllByCategory(String category) {
        return handlerRepository.findAllByCategory(category);
    }

    @Override
    public Flux<Handler> findAllByOrderByIdAsc() {
        return handlerRepository.findAllByOrderByIdAsc();
    }

    @Override
    public Mono<Long> findIdByMinCountGroup(String category) {
        return handlerRepository.findIdByMinCountGroup(category);
    }

    @Override
    public Mono<Integer> updateCountGroupByIdIncrement(Long id) {
        return handlerRepository.updateCountGroupByIdIncrement(id);
    }

    @Override
    public Mono<Integer> updateCountGroupByIdDecrement(Long id) {
        return handlerRepository.updateCountGroupByIdDecrement(id);
    }

    @Override
    public Mono<Handler> findById(Long id) {
        return handlerRepository.findById(id);
    }

    @Override
    public Mono<Handler> save(Handler handler) {
        return handlerRepository.save(handler);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return handlerRepository.deleteById(id);
    }
    
}
