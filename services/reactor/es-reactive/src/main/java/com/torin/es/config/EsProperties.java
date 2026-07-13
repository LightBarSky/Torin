package com.torin.es.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "spring.elasticsearch")
public class EsProperties {
    private String uris;
    private String cert;
    private String username;
    private String password;
    private Boolean https;
}
