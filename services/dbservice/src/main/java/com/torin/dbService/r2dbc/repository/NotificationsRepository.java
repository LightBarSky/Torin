package com.torin.dbService.r2dbc.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import com.torin.dbService.r2dbc.entity.Notifications;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationsRepository extends R2dbcRepository<Notifications, Long> {
    public Flux<Notifications> findByReadFalseOrderByTimestampAsc();

    @Modifying
    @Query("UPDATE systems.notifications SET read = true WHERE id = ANY(:ids)")
    Mono<Integer> markAsRead(@Param("ids") Long[] ids);
 }
