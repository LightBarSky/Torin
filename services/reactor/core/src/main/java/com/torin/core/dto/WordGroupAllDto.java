package com.torin.core.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordGroupAllDto {
    private Long id;

    private Long idGroup;

    private String infoGroup;

    private String titleGroup;

    private String findGroup;

    private String hashGroup;

    private Integer type;

    private Boolean isHandle;

    private Instant lastUpdate;

    private Long linkedId;

    private Long participantsCount;

    private Instant createdDate;
    
    private String flags;

    private String flags2;
    private Instant joinedDate;

    public WordGroupAllDto(String username, String hash) {
        this.findGroup = username;
        this.hashGroup = hash;
    }
}
