package com.torin.prod.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.torin.prod.dto.InputSessionModalDto;
import com.torin.prod.dto.LogEntryDto;
import com.torin.prod.kafka.service.SendNotificationService;

@RestController
@RequestMapping("/api/v1/notify")
public class NotifyController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final SendNotificationService sendNotificationService;

    public NotifyController(SimpMessagingTemplate messagingTemplate, SendNotificationService sendNotificationService) {
        this.messagingTemplate = messagingTemplate;
        this.sendNotificationService = sendNotificationService;
    }

    @PostMapping("/session-request")
    public ResponseEntity<Void> postSessionRequest(
            @RequestBody InputSessionModalDto inputSessionModalDto) {
        messagingTemplate.convertAndSend("/topic/sessionRequest", inputSessionModalDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/fatal-log")
    public ResponseEntity<Void> postFatalLog(
            @RequestBody LogEntryDto logEntryDto) {
        messagingTemplate.convertAndSend("/topic/fatalLog", logEntryDto);
        sendNotificationService.send("warning", String.format("Handler with ID: %s down", logEntryDto.getHandlerId()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/show-qr")
    public ResponseEntity<Void> postShowQr(
            @RequestBody String qrCode) {
        messagingTemplate.convertAndSend("/topic/qrCode", qrCode);
        return ResponseEntity.noContent().build();
    }
}
