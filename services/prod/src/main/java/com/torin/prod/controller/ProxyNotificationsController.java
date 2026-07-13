package com.torin.prod.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.torin.prod.dto.NotificationsDto;
import com.torin.prod.dto.NotificationsIdsDto;
import com.torin.prod.dto.OperationStatusDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/notifications")
public class ProxyNotificationsController {
    @Value("${api.db.url}")
    private String apiDbUrl;
    @Value("${api.telegram.url}")
    private String apiTelegramUrl;
    @Autowired
    private WebClient client;

    @PutMapping
    public Mono<ResponseEntity<OperationStatusDto>> updateNotifications(
            @RequestBody NotificationsIdsDto notificationsIdsDto) {
        return client.put()
                .uri(String.format("%s/api/v1/notifications", apiDbUrl))
                .bodyValue(notificationsIdsDto)
                .retrieve()
                .bodyToMono(OperationStatusDto.class).map(ResponseEntity::ok);
    }

    @GetMapping
    public Flux<NotificationsDto> getNotifications() {
        return client.get()
                .uri(String.format("%s/api/v1/notifications", apiDbUrl))
                .retrieve()
                .bodyToFlux(NotificationsDto.class);
    }
}
