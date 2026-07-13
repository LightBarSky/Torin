package com.torin.core.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record ChatDto(
        Long id,

        @JsonProperty("id_user")
        Long idUser,

        @JsonProperty("id_group")
        Long idGroup,

        @JsonProperty("date_joined")
        Instant dateJoined,

        Long version
) {}
