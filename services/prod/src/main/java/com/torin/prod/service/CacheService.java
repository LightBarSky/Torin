package com.torin.prod.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.torin.prod.dto.HandlerDto;

@Service
public class CacheService {
    private ConcurrentHashMap<Long, HandlerDto> handlers = new ConcurrentHashMap<>();

    public List<HandlerDto> getHandlers() {
        return new ArrayList<>(handlers.values());
    }

    public void updateHandlers(List<HandlerDto> newHandlers) {
        handlers = newHandlers.stream()
                .collect(Collectors.toConcurrentMap(HandlerDto::getId, dto -> dto, (oldDto, newDto) -> newDto, ConcurrentHashMap::new));
    }
}
