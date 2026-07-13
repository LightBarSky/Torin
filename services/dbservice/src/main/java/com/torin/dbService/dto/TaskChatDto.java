package com.torin.dbService.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskChatDto {
    private Long id;

    private Long idChat;

    private Long offsetIdNewMessage;

    private Long offsetIdOldMessage;

    private Instant dateParseUser;

    public Instant dateOfLastRecord;
}
