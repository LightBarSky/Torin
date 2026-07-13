package com.torin.dbService.r2dbc.port;

import com.torin.dbService.r2dbc.entity.Notifications;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationsPort {
    public Flux<Notifications> findByReadFalseOrderByTimestampAsc();

    Mono<Integer> markAsRead(Long[] ids);

    Mono<Notifications> save(Notifications notifications);
}
