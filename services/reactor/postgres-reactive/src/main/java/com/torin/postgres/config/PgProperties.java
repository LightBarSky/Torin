package com.torin.postgres.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "r2dbc")
public class PgProperties {
    private String driver;
    private String host;
    private String port;
    private String user;
    private String password;
    private String database;
}
