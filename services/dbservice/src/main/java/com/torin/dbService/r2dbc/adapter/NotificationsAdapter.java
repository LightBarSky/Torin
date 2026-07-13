package com.torin.dbService.r2dbc.adapter;

import com.torin.dbService.r2dbc.entity.Notifications;
import com.torin.dbService.r2dbc.port.NotificationsPort;
import com.torin.dbService.r2dbc.repository.NotificationsRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class NotificationsAdapter implements NotificationsPort {

    private final NotificationsRepository notificationsRepository;

    public NotificationsAdapter(NotificationsRepository notificationsRepository) {
        this.notificationsRepository = notificationsRepository;
    }

    @Override
    public Flux<Notifications> findByReadFalseOrderByTimestampAsc() {
        return notificationsRepository.findByReadFalseOrderByTimestampAsc();
    }

    @Override
    public Mono<Integer> markAsRead(Long[] ids) {
        return notificationsRepository.markAsRead(ids);
    }

    @Override
    public Mono<Notifications> save(Notifications notifications) {
        return notificationsRepository.save(notifications);
    }
    
}
