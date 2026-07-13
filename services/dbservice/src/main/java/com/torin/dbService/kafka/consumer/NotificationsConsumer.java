package com.torin.dbService.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.torin.dbService.kafka.service.KafkaNotificationsService;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationsConsumer {
    private final KafkaNotificationsService kafkaNotificationsService;

    public NotificationsConsumer(KafkaNotificationsService kafkaNotificationsService) {
        this.kafkaNotificationsService = kafkaNotificationsService;
    }

    @KafkaListener(topics = "${kafka.notifications.listener.topic.name}", groupId = "${kafka.notifications.listener.topic.group-id}")
    public void listen(ConsumerRecord<String, String> record) throws JsonMappingException, JsonProcessingException {
        kafkaNotificationsService.process(record);
    }
}
