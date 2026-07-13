package com.torin.prod.kafka.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torin.prod.dto.HeartBeatDto;

@Service
public class HealthKafkaService {

    private final Map<String, Long> lastSeen = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public HealthKafkaService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void process(ConsumerRecord<String, String> record) {
        try {
            HeartBeatDto heartBeatDto = objectMapper.readValue(record.value(), HeartBeatDto.class);
            lastSeen.put(heartBeatDto.getServiceName(), System.currentTimeMillis());
        } catch (Exception ex) {
            System.out.println("Error in consume heartbeat: " + ex.getMessage());
        }
    }

    public Map<String, Long> getLastSeen() {
        return lastSeen;
    }
}
