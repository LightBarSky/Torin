package com.torin.dbService.r2dbc.service;

import org.springframework.stereotype.Service;

import com.torin.dbService.dto.HandlerDto;
import com.torin.dbService.r2dbc.port.HandlerPort;
import com.torin.dbService.service.MapperService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class HandlerService {

    private final HandlerPort handlerPort;

    private final MapperService mapperService;

    public HandlerService(HandlerPort handlerPort, MapperService mapperService) {
        this.handlerPort = handlerPort;
        this.mapperService = mapperService;
    }

    public Flux<HandlerDto> findAll() {
        return handlerPort.findAllByOrderByIdAsc().map(mapperService::toDto);
    }

    public Mono<HandlerDto> findById(Long id) {
        return handlerPort.findById(id).map(mapperService::toDto);
    }

    public Mono<HandlerDto> addHandler(HandlerDto handlerDto) {
        if (handlerDto.getId() != null) {
            return Mono.error(new IllegalArgumentException("ID must be null for new entity"));
        }
        return handlerPort.save(mapperService.toEntity(handlerDto)).map(mapperService::toDto);
    }

    public Mono<Void> deleteById(Long id) {
        return handlerPort.deleteById(id);
    }

    public Flux<Long> getAllIdByCategory(String category) {
        return handlerPort.findAllByCategory(category)
                .map(handler -> handler.getId());
    }

    public Mono<HandlerDto> updateHandler(Long id, HandlerDto handlerDto) {
        return handlerPort.findById(id)
                .flatMap(exist -> handlerPort.save(mapperService.toEntity(handlerDto)).map(mapperService::toDto));
    }

    public Mono<Long> findIdByMinCountGroupParseGroup() {
        return handlerPort.findIdByMinCountGroup("ParseGroup");
    }

    public Mono<Integer> updateCountGroupByIdIncrement(Long id) {
        return handlerPort.updateCountGroupByIdIncrement(id);
    }

    public Mono<Integer> updateCountGroupByIdDecrement(Long id) {
        return handlerPort.updateCountGroupByIdDecrement(id);
    }
}
