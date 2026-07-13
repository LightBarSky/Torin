package com.torin.prod.service;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.torin.prod.dto.HandlerDto;

@Service
public class RunAllHandlerService {
    private final CacheService cacheService;
    private final SimpMessagingTemplate messagingTemplate;

    public RunAllHandlerService(CacheService cacheService, SimpMessagingTemplate messagingTemplate) {
        this.cacheService = cacheService;
        this.messagingTemplate = messagingTemplate;
    }

    public void start() {
        for (HandlerDto handlerDto : cacheService.getHandlers()) {
            messagingTemplate.convertAndSend("/topic/handler", Map.of("id", handlerDto.getId(),
                "status", "run"));
        }
    }

    public void stop() {
        for (HandlerDto handlerDto : cacheService.getHandlers()) {
            messagingTemplate.convertAndSend("/topic/handler", Map.of("id", handlerDto.getId(),
                "status", "stop"));
        }
    }
}
