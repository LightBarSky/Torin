package com.torin.prod.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.torin.prod.dto.ListenerStatusDto;
import com.torin.prod.dto.LogEntryDto;
import com.torin.prod.kafka.service.ReactiveLogService;
import com.torin.prod.kafka.service.ReactiveStatusListenersService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/logs")
public class LogStreamController {

    private final ReactiveLogService logStreamService;
    private final ReactiveStatusListenersService reactiveStatusListenersService;

    public LogStreamController(ReactiveLogService logStreamService, ReactiveStatusListenersService reactiveStatusListenersService) {
        this.logStreamService = logStreamService;
        this.reactiveStatusListenersService = reactiveStatusListenersService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<LogEntryDto> streamLogs(@RequestParam(required = false) String filter, 
        @RequestParam(required = false) String handlerId) {
        return logStreamService.streamLogs(handlerId, filter);
    }

    @GetMapping(value = "/status-listeners", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ListenerStatusDto> streamStatus() {
        return reactiveStatusListenersService.streamStatus();
    }
}
