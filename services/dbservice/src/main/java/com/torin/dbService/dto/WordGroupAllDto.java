package com.torin.dbService.dto;

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

    private Long idUserJoin;

    private Integer type;

    private Long handlersId;

    private Instant lastUpdate;

    private Instant lastHandle;

    private Integer totalSendRequest = 0;

    private Integer totalDetectPrivate = 0;

    private Long linkedId;

    private Long participantsCount;

    private Instant createdDate;
    
    private String flags;

    private String flags2;
}
