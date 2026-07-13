package com.torin.prod.kafka.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torin.prod.dto.LogEntryDto;
import com.torin.prod.dto.LogLevelFilter;
import com.torin.prod.dto.LogStatusReceive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

@Service
public class ReactiveLogService {
    @Value("${api.db.url}")
    private String apiUrl;
    private final Sinks.Many<ReceiverRecord<String, String>> sink = Sinks.many().multicast().directBestEffort();
    private final Flux<ReceiverRecord<String, String>> flux = sink.asFlux();
    private final ObjectMapper objectMapper;
    private final WebClient client;

    public ReactiveLogService(@Qualifier("logsReceiver") KafkaReceiver<String, String> receiver,
            ObjectMapper objectMapper, WebClient client) {
        this.client = client;
        this.objectMapper = objectMapper;

        receiver.receive()
                .concatMap(record -> Mono.fromRunnable(() -> sink.tryEmitNext(record))
                        .doFinally(signal -> record.receiverOffset().acknowledge())
                        .onErrorResume(e -> {
                            System.err.println("Deserialization error: " + record.value());
                            return Mono.empty();
                        }))
                .subscribe();
    }

    public Flux<LogEntryDto> streamLogs(String handlerId, String filter) {
        LogLevelFilter minLevel = LogLevelFilter.from(filter);
        boolean applyHandlerFilter = handlerId == null ? false : true;
        Flux<LogEntryDto> lastLogs = client.get()
                .uri(String.format("%s/api/v1/log-handler?applyHandlerFilter=%s&handlerId=%s&filter=%s&size=%s",
                        apiUrl, applyHandlerFilter, handlerId, minLevel, 50))
                .retrieve()
                .bodyToFlux(LogEntryDto.class).map(log -> {
                    log.setMode(LogStatusReceive.OLD_MESSAGE.status());
                    return log;
                }).concatWith(Flux.just(new LogEntryDto()).map(log -> {
                    log.setMode(LogStatusReceive.OLD_MESSAGE_OFF.status());
                    return log;
                }));
        return Flux.concat(lastLogs, flux
                .filter(log -> !applyHandlerFilter || handlerId.equals(log.key()))
                .concatMap(record -> Mono
                        .fromCallable(() -> this.objectMapper.readValue(record.value(), LogEntryDto.class)))
                .filter(log -> LogLevelFilter.from(log.level).priority() >= minLevel.priority())
                .map(log -> {
                    log.setMode(LogStatusReceive.NEW_MESSAGE.status());
                    return log;
                }).doOnCancel(() -> System.out.println("Stream closed for handlerId " + handlerId)));
    }
}