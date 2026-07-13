package com.torin.prod.dto;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionsListDto {
    private String text;
    private String value;
    private String lastModified;
    private List<Long> handlersId;
}
