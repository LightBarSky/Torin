package com.torin.dbService.service.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.torin.dbService.dto.TaskChatDto;
import com.torin.dbService.r2dbc.entity.TaskChat;
import com.torin.dbService.r2dbc.repository.TaskChatRepository;
import com.torin.dbService.r2dbc.service.TaskChatsService;

import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)

class TaskChatsServiceIntegrationTest extends AbstractIntegrationDBTest {
    @Autowired
    private TaskChatsService taskChatsService;

    @Autowired
    private TaskChatRepository taskChatRepository;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.locations", () -> "classpath:db/test-migration/taskChats");
    }

    @Test
    void deleteByIdTest() {
        TaskChat taskChat = new TaskChat(null, 123L, -1L, -1L, Instant.now(), Instant.now());

        StepVerifier
                .create(taskChatRepository.save(taskChat).flatMap(saved -> taskChatsService.deleteById(saved.getId())))
                .expectNextCount(0).verifyComplete();
    }

    @Test
    void deleteByIdChatTest() {
        TaskChat taskChat = new TaskChat(null, 1234L, -1L, -1L, Instant.now(), Instant.now());

        StepVerifier
                .create(taskChatRepository.save(taskChat)
                        .flatMap(saved -> taskChatsService.deleteByIdChat(saved.getIdChat())))
                .expectNextCount(0).verifyComplete();
    }

    @Test
    void findByIdChatTest() {
        TaskChat taskChat = new TaskChat(null, 12345L, -1L, -1L, Instant.now(), Instant.now());

        StepVerifier
                .create(taskChatRepository.save(taskChat)
                        .flatMap(saved -> taskChatsService.findByIdChat(saved.getIdChat())))
                .expectNextCount(1).verifyComplete();
    }

    @Test
    void updateTaskChatsIdMismatchShouldFail() {
        TaskChatDto dto = new TaskChatDto(1L, 123456L, -1L, -1L, Instant.now(), Instant.now());

        StepVerifier.create(taskChatsService.updateTaskChats(2L, dto))
                .expectErrorMatches(t -> t instanceof RuntimeException && t.getMessage().equals("Id mismatch"))
                .verify();
    }

    @Test
    void updateTaskChatsTest() {
        TaskChatDto dto = new TaskChatDto(null, 123456L, 0L, 100L, Instant.now(), Instant.now());
        TaskChat taskChat = new TaskChat(null, 123456L, -1L, -1L, Instant.now(), Instant.now());

        StepVerifier.create(taskChatRepository.save(taskChat).flatMap(saved -> {
            dto.setId(saved.getId());
            return taskChatsService.updateTaskChats(saved.getId(), dto)
                    .flatMap(d -> taskChatRepository.findById(saved.getId()));
        })).assertNext(tc -> {
            assertEquals(tc.getId(), dto.getId());
            assertEquals(tc.getIdChat(), dto.getIdChat());
            assertEquals(tc.getOffsetIdNewMessage(), dto.getOffsetIdNewMessage());
            assertEquals(tc.getOffsetIdOldMessage(), dto.getOffsetIdOldMessage());
        }).verifyComplete();
    }

    @Test
    void updateDateParseShouldFail() {
        StepVerifier.create(taskChatsService.updateTaskChatsDateParse(123L, null))
                .expectErrorMatches(t -> t instanceof IllegalArgumentException
                        && t.getMessage().equals("ParseUserDate not must be is null"))
                .verify();
    }

    @Test
    void updateDateParseTest() {
        TaskChat taskChat = new TaskChat(null, 1234567L, -1L, -1L, Instant.now(), Instant.now());
        Instant instantUpd = Instant.parse("2025-06-02T12:15:00Z");
        StepVerifier
                .create(taskChatRepository.save(taskChat)
                        .flatMap(saved -> taskChatsService.updateTaskChatsDateParse(saved.getIdChat(), instantUpd)
                                .flatMap(i -> taskChatRepository.findById(saved.getId()))))
                .assertNext(tc -> {
                    assertEquals(tc.getDateParseUser(), instantUpd);
                }).verifyComplete();
    }

    @Test
    void addTaskChatWithIdShouldFail() {
        TaskChatDto dto = new TaskChatDto(1L, 123456L, 0L, 100L, Instant.now(), Instant.now());
        StepVerifier.create(taskChatsService.addTaskChats(dto)).expectErrorMatches(
                t -> t instanceof IllegalArgumentException && t.getMessage().equals("ID must be null for new entity"))
                .verify();
    }

    @Test
    void addTaskChat() {
        TaskChatDto dto = new TaskChatDto(null, 12345678L, 0L, 100L, Instant.now(), Instant.now());
        StepVerifier.create(taskChatsService.addTaskChats(dto)).assertNext(t -> {
            assertNotNull(t.getId());
        }).verifyComplete();
    }
}
