package com.torin.dbService.dto;

import java.time.Instant;

public record UserDto(
        Long idUser,
        String firstName,
        String lastName,
        String username,
        String number,
        String userPhoto,
        String pgTags,
        Boolean isGeo,
        Instant updatedAt,
        String birthday,
        String flags,
        String flags2,
        String flagsFull,
        String flags2Full,
        String about,
        Boolean isBot,
        String botInfo,
        Long personalChannelId,
        String locationAddress,
        Double locationLat,
        Double locationLon,
        Integer locationRadius) {
}
