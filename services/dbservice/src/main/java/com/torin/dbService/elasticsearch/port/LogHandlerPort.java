package com.torin.dbService.elasticsearch.port;

import com.torin.dbService.dto.LogEntryDto;
import com.torin.dbService.dto.LogLevelFilter;

import reactor.core.publisher.Flux;

public interface LogHandlerPort {

    Flux<LogEntryDto> getLastLogs(
            boolean applyHandlerFilter,
            String handlerId,
            LogLevelFilter minLevel,
            int size);
}
