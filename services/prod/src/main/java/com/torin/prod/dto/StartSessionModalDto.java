package com.torin.prod.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartSessionModalDto {
    private String apiId;
    private String hash;
    private String phoneNumber;
    private String withQR;
}
