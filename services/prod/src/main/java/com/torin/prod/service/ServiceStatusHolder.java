package com.torin.prod.service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.torin.prod.config.ServicesConfig;
import com.torin.prod.dto.*;
import com.torin.prod.kafka.service.SendNotificationService;

@Component
@EnableConfigurationProperties(ServicesConfig.class)
public class ServiceStatusHolder {
        private final SimpMessagingTemplate messagingTemplate;
        private final ConcurrentHashMap<String, Boolean> serviceAvailability = new ConcurrentHashMap<>();
        private final SendNotificationService sendNotificationService;
        private final ServicesConfig config;

        public ServiceStatusHolder(SimpMessagingTemplate messagingTemplate, SendNotificationService sendNotificationService, ServicesConfig config) {
                this.messagingTemplate = messagingTemplate;
                this.sendNotificationService = sendNotificationService;
                this.config = config;
                for (String node : config.getNodes().keySet()) {
                        serviceAvailability.put(node, false);
                }
        }

        public void setStatus(String serviceName, boolean availability) throws JsonProcessingException {
                if ((!serviceAvailability.containsKey(serviceName) && availability) ||
                                (serviceAvailability.containsKey(serviceName) && !serviceAvailability.get(serviceName)
                                                && availability)) {
                        sendNotificationService.send("info", String.format("Connection established with %s",
                                        config.getNodes().get(serviceName).getName()));
                } else if ((!serviceAvailability.containsKey(serviceName) && !availability) ||
                                (serviceAvailability.containsKey(serviceName) && serviceAvailability.get(serviceName)
                                                && !availability)) {

                        sendNotificationService.send("error", String.format("Connection to %s lost",
                                        config.getNodes().get(serviceName).getName()));
                }
                serviceAvailability.put(serviceName, availability);
                var status = new StatusDto(serviceName, availability);
                messagingTemplate.convertAndSend("/topic/checkService", status);
        }

        public boolean isAvailability(String serviceName) {
                return serviceAvailability.getOrDefault(serviceName, false);
        }

        public Map<String, Boolean> GetAllStatuses() {
                return Collections.unmodifiableMap(serviceAvailability);
        }
}
