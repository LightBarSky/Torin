package com.torin.core.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WordGroupAllChangedDto(
                Long id,

                @JsonProperty("id_group") Long idGroup,

                @JsonProperty("info_group") String infoGroup,

                @JsonProperty("title_group") String titleGroup,

                @JsonProperty("find_group") String findGroup,

                @JsonProperty("hash_group") String hashGroup,

                Integer type,

                Instant date,

                @JsonProperty("linked_id") Long linkedId,

                String flags,
                String flags2) {
}