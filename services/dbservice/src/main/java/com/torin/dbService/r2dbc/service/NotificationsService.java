package com.torin.dbService.r2dbc.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.torin.dbService.dto.NotificationsDto;
import com.torin.dbService.r2dbc.port.NotificationsPort;
import com.torin.dbService.service.MapperService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class NotificationsService {
    private final NotificationsPort notificationsPort;
    private final MapperService mapperService;

    public NotificationsService(NotificationsPort notificationsPort, MapperService mapperService) {
        this.notificationsPort = notificationsPort;
        this.mapperService = mapperService;
    }

    public Mono<NotificationsDto> addNotifications(NotificationsDto notificationsDto) {
        if (notificationsDto.id() != null) {
            return Mono.error(new IllegalArgumentException("ID must be null for new entity"));
        }
        return notificationsPort.save(mapperService.toEntity(notificationsDto)).map(mapperService::toDto);
    }

    public Mono<Integer> updateNotifications(List<Long> ids) {
        return notificationsPort.markAsRead(ids.toArray(new Long[0]));
    }

    public Flux<NotificationsDto> getNotifications() {
        return notificationsPort.findByReadFalseOrderByTimestampAsc().map(mapperService::toDto);
    } 
}
