package com.torin.dbService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.torin.dbService.dto.LogEntryDto;
import com.torin.dbService.dto.LogLevelFilter;
import com.torin.dbService.elasticsearch.service.LogHandlerService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("api/v1/log-handler")
public class LogHandlerController {
    @Autowired
    private LogHandlerService logHandlerService;

    @GetMapping
    public Flux<LogEntryDto> getNotifications(@RequestParam Boolean applyHandlerFilter,
            @RequestParam(required = false) String handlerId, @RequestParam(required = false) LogLevelFilter filter,
            @RequestParam(defaultValue = "50") int size) {
        if (filter == null) {
            filter = LogLevelFilter.INFO;
        }
        if (applyHandlerFilter && handlerId == null) {
            return Flux.error(new IllegalArgumentException("handlerId is required when applyHandlerFilter=true"));
        }
        return logHandlerService.getLastLogs(applyHandlerFilter, handlerId, filter, size);
    }
}
