package com.torin.dbService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandlerDto {
    private Long id;

    private Long apiId;

    private String hash;

    private String phone;

    private String directoryForUserPhoto;

    private String directoryForMedia;

    private String category;

    private Integer countGroup;

    private String nameHandler;
}