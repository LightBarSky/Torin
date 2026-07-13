package com.torin.dbService.r2dbc.adapter;

import com.torin.dbService.r2dbc.entity.Notifications;
import com.torin.dbService.r2dbc.port.NotificationsPort;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class NoOpNotificationsAdapter implements NotificationsPort {

    @Override
    public Flux<Notifications> findByReadFalseOrderByTimestampAsc() {
        return Flux.empty();
    }

    @Override
    public Mono<Integer> markAsRead(Long[] ids) {
        return Mono.empty();
    }

    @Override
    public Mono<Notifications> save(Notifications notifications) {
        return Mono.empty();
    }
    
}
