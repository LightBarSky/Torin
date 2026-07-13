package com.torin.prod.kafka.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torin.prod.dto.ListenerStatusDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;

@Service
public class ReactiveStatusListenersService {
    private final Sinks.Many<ListenerStatusDto> sink = Sinks.many().multicast().directBestEffort();
    private final ObjectMapper objectMapper;

    public ReactiveStatusListenersService(@Qualifier("statusReceiver") KafkaReceiver<String, String> receiver,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        receiver.receive()
                .publishOn(Schedulers.boundedElastic())
                .flatMap(record -> Mono.fromCallable(() -> this.objectMapper.readValue(record.value(), ListenerStatusDto.class))
                        .doOnNext(stat -> sink.tryEmitNext(stat))
                        .doFinally(signal -> record.receiverOffset().acknowledge())
                        .onErrorResume(e -> {
                            System.err.println("Deserialization error: " + record.value());
                            return Mono.empty();
                        }))
                .subscribe();
    }
    
    public Flux<ListenerStatusDto> streamStatus() {
        return sink.asFlux();
    }
}
