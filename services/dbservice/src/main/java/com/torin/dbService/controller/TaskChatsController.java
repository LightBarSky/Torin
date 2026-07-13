package com.torin.dbService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.torin.dbService.dto.PatchParseUserRequest;
import com.torin.dbService.dto.TaskChatDto;
import com.torin.dbService.r2dbc.service.TaskChatsService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/task-chats")
public class TaskChatsController {
    @Autowired
    private TaskChatsService taskChatsService;

    @GetMapping("/{idChat}")
    public Mono<ResponseEntity<TaskChatDto>> getTaskChatsByIdChat(@PathVariable Long idChat) {
        return taskChatsService.findByIdChat(idChat).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<TaskChatDto>> postTaskChats(@RequestBody TaskChatDto taskChatDto) {
        return taskChatsService.addTaskChats(taskChatDto).map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<TaskChatDto>> putTaskChats(@PathVariable Long id,
            @RequestBody TaskChatDto taskChatDto) {
        if (!taskChatDto.getId().equals(id)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return taskChatsService.updateTaskChats(id, taskChatDto).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{idChat}/date-parse-user")
    public Mono<ResponseEntity<Integer>> updateDateParseUser(@PathVariable Long idChat,
            @RequestBody PatchParseUserRequest request) {
        
        return taskChatsService.updateTaskChatsDateParse(idChat, request.parseUser()).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
