package com.torin.dbService.r2dbc.service;

import java.time.Instant;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.torin.dbService.dto.TaskChatDto;
import com.torin.dbService.r2dbc.port.TaskChatPort;
import com.torin.dbService.service.MapperService;

import reactor.core.publisher.Mono;

@Service
public class TaskChatsService {
    
    private TaskChatPort taskChatPort;
    
    private MapperService mapperService;

    public TaskChatsService(TaskChatPort taskChatPort, MapperService mapperService) {
        this.taskChatPort = taskChatPort;
        this.mapperService = mapperService;
    }

    public Mono<Void> deleteById(Long id) {
        return taskChatPort.deleteById(id);
    }

    public Mono<Void> deleteByIdChat(Long idChat) {
        return taskChatPort.deleteByIdChat(idChat);
    }

    public Mono<TaskChatDto> findByIdChat(Long idChat) {
        return taskChatPort.findByIdChat(idChat).map(mapperService::toDto);
    }

    public Mono<TaskChatDto> updateTaskChats(Long id, TaskChatDto taskChatDto) {
        if (!Objects.equals(id, taskChatDto.getId())) {
            return Mono.error(new IllegalArgumentException("Id mismatch"));
        }
        return taskChatPort.findById(id)
                .flatMap(entity -> taskChatPort.save(mapperService.toEntity(taskChatDto)))
                .map(mapperService::toDto);
    }

    public Mono<Integer> updateTaskChatsDateParse(Long idChat, Instant dateParseUser) {
        if (dateParseUser == null) {
            return Mono.error(new IllegalArgumentException("ParseUserDate not must be is null"));
        }
        return taskChatPort.updateLastParseUser(idChat, dateParseUser);
    }

    public Mono<TaskChatDto> addTaskChats(TaskChatDto taskChatDto) {
        if (taskChatDto.getId() != null) {
            return Mono.error(new IllegalArgumentException("ID must be null for new entity"));
        }
        return taskChatPort.findByIdChat(taskChatDto.getIdChat()).map(mapperService::toDto)
                .switchIfEmpty(taskChatPort.save(mapperService.toEntity(taskChatDto)).map(mapperService::toDto));
    }
}
