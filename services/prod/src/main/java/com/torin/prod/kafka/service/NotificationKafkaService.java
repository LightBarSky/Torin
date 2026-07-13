package com.torin.prod.kafka.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.torin.prod.dto.NotificationsDto;

@Service
public class NotificationKafkaService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ObjectMapper objectMapper;


    public void process(ConsumerRecord<String, String> record) throws JsonMappingException, JsonProcessingException {
        NotificationsDto notificationsDto = objectMapper.readValue(record.value(), NotificationsDto.class);
        messagingTemplate.convertAndSend("/topic/notifications", notificationsDto);
    }
}
