package com.torin.dbService.kafka.port;

public interface KafkaSendPort {
    void send(String topic, String key, String payload);
}
