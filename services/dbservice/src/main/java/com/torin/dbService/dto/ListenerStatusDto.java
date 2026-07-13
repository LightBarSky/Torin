package com.torin.dbService.dto;

public record ListenerStatusDto(
        ListenerStatusSubDto statUsers,
        ListenerStatusSubDto statChats,
        ListenerStatusSubDto statAdminChats,
        ListenerStatusSubDto statGifts,
        ListenerStatusSubDto statReaction,
        ListenerStatusSubDto statReactionGeneral,
        ListenerStatusSubDto statMessages,
        ListenerStatusSubDto statMessagesProp,
        ListenerStatusSubDto statMessagesEntet,
        ListenerStatusSubDto statTaskChats,
        ListenerStatusSubDto statWordGroupAll) {
    public ListenerStatusDto() {
        this(
                new ListenerStatusSubDto(),
                new ListenerStatusSubDto(),
                new ListenerStatusSubDto(),
                new ListenerStatusSubDto(),
                new ListenerStatusSubDto(),
                new ListenerStatusSubDto(),
                new ListenerStatusSubDto(),
                new ListenerStatusSubDto(),
                new ListenerStatusSubDto(),
                new ListenerStatusSubDto(),
                new ListenerStatusSubDto());
    }
}
