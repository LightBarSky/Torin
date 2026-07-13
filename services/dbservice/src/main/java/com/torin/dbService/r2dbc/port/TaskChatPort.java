package com.torin.dbService.r2dbc.port;

import java.time.Instant;

import com.torin.dbService.r2dbc.entity.TaskChat;

import reactor.core.publisher.Mono;

public interface TaskChatPort {
    Mono<Void> deleteByIdChat(Long idChat);

    Mono<Void> deleteById(Long id);

    Mono<TaskChat> findByIdChat(Long idChat);

    Mono<TaskChat> findById(Long id);

    Mono<TaskChat> save(TaskChat taskChat);

    Mono<Boolean> existsByIdChat(Long idChat);

    Mono<Integer> updateLastParseUser(
            Long idChat, Instant date);

}
