package com.torin.dbService.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "kafka-to-db")
public class KafkaToDbProperties {

    private int userSize;
    private int chatSize;
    private int adminChatsSize;
    private int giftsSize;
    private int reactionsSize;
    private int reactionsGeneralSize;
    private int messagesSize;
    private int messagesPropertiesSize;
    private int taskChatsSize;
    private int wordGroupAllSize;
    private int messagesEntitiesSize;
    private String topicForLogs;
    private String topicForStatus;
    private String keyForLogs;

    private Kafka kafka;

    public static class Kafka {
        private String bootstrapServers;
        private Map<String, Object> consumer = new HashMap<>();
        private Map<String, Object> listener = new HashMap<>();

        public String getBootstrapServers() {
            return bootstrapServers;
        }

        public void setBootstrapServers(String bootstrapServer) {
            this.bootstrapServers = bootstrapServer;
        }

        public Map<String, Object> getConsumer() {
            return consumer;
        }

        public void setConsumer(Map<String, Object> consumer) {
            this.consumer = consumer;
        }

        public Map<String, Object> getListener() {
            return listener;
        }

        public void setListener(Map<String, Object> listener) {
            this.listener = listener;
        }
    }
}
