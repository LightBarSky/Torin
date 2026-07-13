package com.torin.dbService.r2dbc.repository;

import java.time.Instant;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import com.torin.dbService.r2dbc.entity.TaskChat;

import reactor.core.publisher.Mono;

public interface TaskChatRepository extends R2dbcRepository<TaskChat, Long> {
    Mono<Void> deleteByIdChat(Long idChat);

    Mono<TaskChat> findByIdChat(Long idChat);

    Mono<Boolean> existsByIdChat(Long idChat);

    @Modifying
    @Query("""
                update base_handler.task_chats
                set date_parse_user = :date
                where id_chat = :idChat
            """)
    Mono<Integer> updateLastParseUser(
            @Param("idChat") Long idChat,
            @Param("date") Instant date);
}
