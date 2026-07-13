package com.torin.dbService.r2dbc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "r2dbc")
public class R2dbcProperties {
    private String host;

    private Integer port;

    private String dbName;

    private String driver;

    private String username;

    private String password;
}
