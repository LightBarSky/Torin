package com.torin.dbService.r2dbc.adapter;

import java.time.Instant;

import com.torin.dbService.r2dbc.entity.TaskChat;
import com.torin.dbService.r2dbc.port.TaskChatPort;

import reactor.core.publisher.Mono;

public class NoOpTaskChatAdapter implements TaskChatPort {

    @Override
    public Mono<Void> deleteByIdChat(Long idChat) {
        return Mono.empty();
    }

    @Override
    public Mono<TaskChat> findByIdChat(Long idChat) {
        return Mono.empty();
    }

    @Override
    public Mono<Boolean> existsByIdChat(Long idChat) {
        return Mono.empty();
    }

    @Override
    public Mono<Integer> updateLastParseUser(Long idChat, Instant date) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return Mono.empty();
    }

    @Override
    public Mono<TaskChat> findById(Long id) {
        return Mono.empty();
    }

    @Override
    public Mono<TaskChat> save(TaskChat taskChat) {
        return Mono.empty();
    }

}
