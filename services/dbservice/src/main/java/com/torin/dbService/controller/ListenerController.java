package com.torin.dbService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.torin.dbService.dto.OperationStatusDto;
import com.torin.dbService.kafka.service.KafkaToDbService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("api/v1/listener")
public class ListenerController {
    @Autowired
    private KafkaToDbService kafkaToDbService;

    @PostMapping("/start")
    public Mono<ResponseEntity<OperationStatusDto>> startListener() {

        return Mono.fromCallable(() -> {
            kafkaToDbService.startListeners();
            return true;
        }).subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(
                        ResponseEntity.ok(
                                new OperationStatusDto(true, "Successfully"))))
                .onErrorResume(t -> Mono.just(
                        ResponseEntity.badRequest()
                                .body(new OperationStatusDto(false, t.getMessage()))));
    }

    @PostMapping("/stop")
    public Mono<ResponseEntity<OperationStatusDto>> stopListener() {
        return Mono.fromCallable(() -> {
            kafkaToDbService.stopListeners();
            return true;
        }).subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(
                        ResponseEntity.ok(
                                new OperationStatusDto(true, "Successfully"))))
                .onErrorResume(t -> Mono.just(
                        ResponseEntity.badRequest()
                                .body(new OperationStatusDto(false, t.getMessage()))));
    }

    @GetMapping("/running")
    public Mono<ResponseEntity<Boolean>> runningListener() {
        return Mono.fromCallable(() -> kafkaToDbService.getRunning()).subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok);
    }
}
