package com.torin.dbService.kafka.adapter;

import org.springframework.kafka.core.KafkaTemplate;

import com.torin.dbService.kafka.port.KafkaSendPort;

public class KafkaSendAdapter implements KafkaSendPort {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaSendAdapter(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topic, String key, String payload) {
        kafkaTemplate.send(topic, key, payload);
    }
}
