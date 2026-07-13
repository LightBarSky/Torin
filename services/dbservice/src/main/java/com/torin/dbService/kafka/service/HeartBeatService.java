package com.torin.dbService.kafka.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.torin.dbService.dto.HeartBeatDto;
import com.torin.dbService.kafka.port.KafkaSendPort;

@EnableScheduling
@Component
public class HeartBeatService {
    @Value("${kafka.heartbeat.topic.name}")
    private String heartBeatTopic;
    @Value("${spring.application.name}")
    private String nameService;
    private final KafkaSendPort kafkaSendPort;
    private final ObjectMapper objectMapper;

    public HeartBeatService(KafkaSendPort kafkaSendPort, ObjectMapper objectMapper) {
        this.kafkaSendPort = kafkaSendPort;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRate = 3000)
    public void sendHeartbeat() throws JsonProcessingException {
        kafkaSendPort.send(heartBeatTopic, null, objectMapper.writeValueAsString(new HeartBeatDto(nameService, Instant.now())));
    }
}
