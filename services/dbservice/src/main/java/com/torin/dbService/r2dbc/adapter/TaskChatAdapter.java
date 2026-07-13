package com.torin.dbService.r2dbc.adapter;

import java.time.Instant;

import com.torin.dbService.r2dbc.entity.TaskChat;
import com.torin.dbService.r2dbc.port.TaskChatPort;
import com.torin.dbService.r2dbc.repository.TaskChatRepository;

import reactor.core.publisher.Mono;

public class TaskChatAdapter implements TaskChatPort {

    private final TaskChatRepository taskChatRepository;

    public TaskChatAdapter(TaskChatRepository taskChatRepository) {
        this.taskChatRepository = taskChatRepository;
    }

    @Override
    public Mono<Void> deleteByIdChat(Long idChat) {
        return taskChatRepository.deleteByIdChat(idChat);
    }

    @Override
    public Mono<TaskChat> findByIdChat(Long idChat) {
        return taskChatRepository.findByIdChat(idChat);
    }

    @Override
    public Mono<Boolean> existsByIdChat(Long idChat) {
        return taskChatRepository.existsByIdChat(idChat);
    }

    @Override
    public Mono<Integer> updateLastParseUser(Long idChat, Instant date) {
        return taskChatRepository.updateLastParseUser(idChat, date);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return taskChatRepository.deleteById(id);
    }

    @Override
    public Mono<TaskChat> findById(Long id) {
        return taskChatRepository.findById(id);
    }

    @Override
    public Mono<TaskChat> save(TaskChat taskChat) {
        return taskChatRepository.save(taskChat);
    }
}
