package com.torin.dbService.kafka.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.torin.dbService.dto.ListenerStatusDto;
import com.torin.dbService.dto.LogEntryDto;
import com.torin.dbService.kafka.config.KafkaToDbProperties;
import com.torin.dbService.kafka.port.KafkaSendPort;

@Service
@EnableConfigurationProperties(KafkaToDbProperties.class)
public class SendLogKafkaToDbService {
    private final KafkaSendPort kafkaSendPort;
    @Autowired
    private ObjectMapper objectMapper;

    private final KafkaToDbProperties kafkaToDbProperties;

    public ListenerStatusDto status = new ListenerStatusDto();

    public SendLogKafkaToDbService(KafkaSendPort kafkaSendPort, KafkaToDbProperties kafkaToDbProperties) {
        this.kafkaSendPort = kafkaSendPort;
        this.kafkaToDbProperties = kafkaToDbProperties;
    }

    public void sendLog(String message, String level) throws JsonProcessingException {
        kafkaSendPort.send(kafkaToDbProperties.getTopicForLogs(), kafkaToDbProperties.getKeyForLogs(),
                objectMapper.writeValueAsString(
                        LogEntryDto.create(
                                kafkaToDbProperties.getKeyForLogs(),
                                message,
                                level, Instant.now())));
        kafkaSendPort.send(kafkaToDbProperties.getTopicForStatus(), kafkaToDbProperties.getKeyForLogs(),
                objectMapper.writeValueAsString(status));
    }

    public void clearStatus() {
        this.status = new ListenerStatusDto();
    }
}
