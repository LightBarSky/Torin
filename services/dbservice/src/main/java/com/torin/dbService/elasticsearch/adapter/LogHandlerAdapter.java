package com.torin.dbService.elasticsearch.adapter;

import java.util.Collections;

import org.springframework.data.domain.*;

import com.torin.dbService.dto.LogEntryDto;
import com.torin.dbService.dto.LogLevelFilter;
import com.torin.dbService.elasticsearch.port.LogHandlerPort;
import com.torin.dbService.elasticsearch.repository.LogHandlerRepository;

import reactor.core.publisher.Flux;

public class LogHandlerAdapter implements LogHandlerPort {
    private final LogHandlerRepository repository;

    public LogHandlerAdapter(LogHandlerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<LogEntryDto> getLastLogs(boolean applyHandlerFilter, String handlerId, LogLevelFilter minLevel,
            int size) {
        Pageable pageable = PageRequest.of(0, size);
        if (applyHandlerFilter) {
            return repository
                    .findByHandlerIdAndLevelInOrderByTimestampDesc(handlerId, minLevel.esLevels(),
                            pageable)
                    .collectList()
                    .flatMapMany(list -> {
                        Collections.reverse(list);
                        return Flux.fromIterable(
                                list.stream()
                                        .map(log -> LogEntryDto.create(
                                                log.getHandlerId(),
                                                log.getMessage(),
                                                log.getLevel(),
                                                log.getTimestamp()))
                                        .toList());
                    });
        } else {
            return repository.findByLevelInOrderByTimestampDesc(minLevel.esLevels(), pageable)
                    .collectList()
                    .flatMapMany(list -> {
                        Collections.reverse(list);
                        return Flux.fromIterable(
                                list.stream()
                                        .map(log -> LogEntryDto.create(
                                                log.getHandlerId(),
                                                log.getMessage(),
                                                log.getLevel(),
                                                log.getTimestamp()))
                                        .toList());
                    });
        }
    }
}
