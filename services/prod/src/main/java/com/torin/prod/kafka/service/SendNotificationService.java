package com.torin.prod.kafka.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.torin.prod.dto.NotificationsDto;
import com.torin.prod.kafka.port.KafkaSendPort;

@Service
public class SendNotificationService {
    @Autowired
    private KafkaSendPort kafkaSendPort;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${kafka.notifications.topic.name}")
    private String topicNameNotifications;

    public void send(String type, String message) {
        try {
            var notif = new NotificationsDto(Instant.now(), type, message);
            kafkaSendPort.send(topicNameNotifications, objectMapper.writeValueAsString(notif));
        } catch (JsonProcessingException je) {
            System.out.println("Error send notifications: " + je);
        }
    }
}
