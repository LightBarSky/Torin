package com.torin.prod.kafka.port;

public interface KafkaSendPort {
    void send(String topic, String payload);
}
