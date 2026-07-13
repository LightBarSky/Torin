package com.torin.dbService.kafka.service;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.torin.dbService.dto.NotificationsDto;
import com.torin.dbService.kafka.port.KafkaSendPort;
import com.torin.dbService.r2dbc.service.NotificationsService;

@Service
public class KafkaNotificationsService {
    private final ObjectMapper objectMapper;
    private final NotificationsService notificationsService;
    private final KafkaSendPort kafkaSendPort;
    @Value("${kafka.notifications.send.topic.name}")
    private String sendTopicNameNotif;

    public KafkaNotificationsService(NotificationsService notificationsService, KafkaSendPort kafkaSendPort, ObjectMapper objectMapper) {
        this.notificationsService = notificationsService;
        this.kafkaSendPort = kafkaSendPort;
        this.objectMapper = objectMapper;
    }

    public void process(ConsumerRecord<String, String> record) throws JsonMappingException, JsonProcessingException {
        NotificationsDto notificationsDto = objectMapper.readValue(record.value(), NotificationsDto.class);

        NotificationsDto notifications = notificationsService.addNotifications(notificationsDto).block();
        kafkaSendPort.send(sendTopicNameNotif, null, objectMapper.writeValueAsString(notifications));
    }
}
