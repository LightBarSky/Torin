package com.torin.prod.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.torin.prod.kafka.service.NotificationKafkaService;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaNotificationConsumer {
    private final NotificationKafkaService notificationKafkaService;

    public KafkaNotificationConsumer(NotificationKafkaService notificationKafkaService) {
        this.notificationKafkaService = notificationKafkaService;
    }

    @KafkaListener(topics = "${kafka.gui-notifications.topic.name}", groupId = "${kafka.gui-notifications.group-id}", properties = {
            "auto.offset.reset=latest"
    })
    public void listener(ConsumerRecord<String, String> record) throws JsonMappingException, JsonProcessingException {
        notificationKafkaService.process(record);
    }
}
