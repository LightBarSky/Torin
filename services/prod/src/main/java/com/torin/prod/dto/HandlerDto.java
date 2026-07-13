package com.torin.prod.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HandlerDto {
    private Long id;

    private Long apiId;

    private String hash;

    private String phone;

    private String directoryForUserPhoto;

    private String directoryForMedia;

    private String category;

    private String hashAccessPath;

    private String nameHandler;

    @JsonIgnore
    private HandlerStatus status = HandlerStatus.Stopped;
    
    private String warning;
}
