package com.torin.dbService.elasticsearch.service;

import org.springframework.stereotype.Service;

import com.torin.dbService.dto.LogEntryDto;
import com.torin.dbService.dto.LogLevelFilter;
import com.torin.dbService.elasticsearch.port.LogHandlerPort;

import reactor.core.publisher.Flux;

@Service
public class LogHandlerService {
        private final LogHandlerPort logHandlerPort;

        public LogHandlerService(LogHandlerPort logHandlerPort) {
                this.logHandlerPort = logHandlerPort;
        }

        public Flux<LogEntryDto> getLastLogs(boolean applyHandlerFilter, String handlerId, LogLevelFilter minLevel,
                        int size) {
                return logHandlerPort.getLastLogs(applyHandlerFilter, handlerId, minLevel, size);
        }
}
