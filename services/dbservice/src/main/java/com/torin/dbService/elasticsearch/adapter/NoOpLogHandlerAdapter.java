package com.torin.dbService.elasticsearch.adapter;


import com.torin.dbService.dto.LogEntryDto;
import com.torin.dbService.dto.LogLevelFilter;
import com.torin.dbService.elasticsearch.port.LogHandlerPort;

import reactor.core.publisher.Flux;

public class NoOpLogHandlerAdapter implements LogHandlerPort {
    @Override
    public Flux<LogEntryDto> getLastLogs(boolean applyHandlerFilter, String handlerId,
            LogLevelFilter minLevel, int size) {
        return Flux.empty();
    }
}
