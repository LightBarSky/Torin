package com.torin.dbService.dto;

import java.time.Instant;

public record MessageDto(
        Long idGroup,
        Long isComments,
        Long replyToPost,
        Long idMessage,
        Long idUser,
        Long idGroupedMessage,
        String contentText,
        String contentMedia,
        Long idReply,
        Instant date) {
}
