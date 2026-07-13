package com.torin.prod.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.torin.prod.kafka.service.HealthKafkaService;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaHeartbeatConsumer {
    private final HealthKafkaService healthKafkaService;

    public KafkaHeartbeatConsumer(HealthKafkaService healthKafkaService) {
        this.healthKafkaService = healthKafkaService;
    }

    @KafkaListener(topics = "${kafka.heartbeat.topic.name}", groupId = "${kafka.heartbeat.group-id}")
    public void consume(ConsumerRecord<String, String> record) {
        healthKafkaService.process(record);
    }
}
