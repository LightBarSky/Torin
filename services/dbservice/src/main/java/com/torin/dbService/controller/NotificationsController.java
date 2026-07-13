package com.torin.dbService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.torin.dbService.dto.NotificationsDto;
import com.torin.dbService.dto.NotificationsIdsDto;
import com.torin.dbService.dto.OperationStatusDto;
import com.torin.dbService.r2dbc.service.NotificationsService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/notifications")
public class NotificationsController {
    @Autowired
    private NotificationsService notificationsService;

    @GetMapping
    public Flux<NotificationsDto> getNotifications() {
        return notificationsService.getNotifications();
    }

    @PutMapping
    public Mono<ResponseEntity<OperationStatusDto>> putNotifications(
            @RequestBody NotificationsIdsDto notificationsIds) {
        return notificationsService.updateNotifications(notificationsIds.ids())
                .map(x -> ResponseEntity.ok(new OperationStatusDto(true, String.format("Обновлено %s записей", x))));

    }
}
