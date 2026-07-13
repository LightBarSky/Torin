package com.torin.prod.kafka.config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
@ConfigurationProperties(prefix = "kafka")
public class KafkaConfig {

    private String bootstrapServers;
    private Consumer consumer;
    private Topics topics;

    @Data
    public static class Consumer {
        private String keyDeserializer;
        private String valueDeserializer;
        private String autoOffsetReset;
    }

    @Data
    public static class Topics {
        private Topic logs;
        private Topic status;
    }

    @Data
    public static class Topic {
        private String name;
        private String groupId;
    }
}
