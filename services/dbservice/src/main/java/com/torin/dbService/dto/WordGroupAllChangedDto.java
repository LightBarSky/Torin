package com.torin.dbService.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordGroupAllChangedDto {

    private Long id;

    private Long idGroup;

    private String infoGroup;

    private String titleGroup;

    private String findGroup;

    private String hashGroup;

    private Integer type;

    private Instant date = Instant.now();

    private Long linkedId;
    
    private String flags;

    private String flags2;
}
