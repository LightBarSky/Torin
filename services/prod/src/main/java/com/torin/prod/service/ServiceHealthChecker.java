package com.torin.prod.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.torin.prod.kafka.service.HealthKafkaService;

@Component
@EnableScheduling
public class ServiceHealthChecker {

    private final ServiceStatusHolder statusHolder;
    private final HealthKafkaService healthKafkaService;
    @Value("${timeout_heartbeat}")
    private Long timeoutHeartbeat;

    public ServiceHealthChecker(ServiceStatusHolder statusHolder, HealthKafkaService healthKafkaService) {
        this.statusHolder = statusHolder;
        this.healthKafkaService = healthKafkaService;
    }

    

    @Scheduled(fixedDelay = 2000)
    public void checkService() {
        long now = System.currentTimeMillis();
        healthKafkaService.getLastSeen().forEach((serviceName, lastTime) -> {
            boolean alive = now - lastTime < timeoutHeartbeat;
            try {
                statusHolder.setStatus(serviceName, alive);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }
}
