package com.torin.prod.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("services")
public class ServicesConfig {
    private Map<String, ServiceNode> nodes;

    @Data
    public static class ServiceNode {
        private String name;
        private String key;
    }
}
