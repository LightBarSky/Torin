package com.torin.dbService.dto;

import java.util.List;

public record NotificationsIdsDto(List<Long> ids) {
    public NotificationsIdsDto {
        ids = ids == null ? List.of() : List.copyOf(ids);
    }
}
