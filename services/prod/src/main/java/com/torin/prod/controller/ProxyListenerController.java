package com.torin.prod.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.torin.prod.dto.OperationStatusDto;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/listener")
public class ProxyListenerController {
    @Value("${api.db.url}")
    private String apiDbUrl;
    @Value("${api.telegram.url}")
    private String apiTelegramUrl;
    @Autowired
    private WebClient client;

    @PostMapping("/start")
    public Mono<ResponseEntity<OperationStatusDto>> startListener() {

        return client.post()
                .uri(String.format("%s/api/v1/listener/start", apiDbUrl))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(OperationStatusDto.class).map(ResponseEntity::ok);
                    }
                    return response.bodyToMono(OperationStatusDto.class)
                            .map(body -> ResponseEntity
                                    .status(response.statusCode())
                                    .body(body));
                }).onErrorResume(WebClientResponseException.class, ex -> Mono.just(
                        ResponseEntity.status(ex.getStatusCode())
                                .body(ex.getResponseBodyAs(OperationStatusDto.class))))
                .onErrorResume(Exception.class, ex -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new OperationStatusDto(false, "Внутренняя ошибка: " + ex.getMessage()))));
    }

    @PostMapping("/stop")
    public Mono<ResponseEntity<OperationStatusDto>> stopListener() {

        return client.post()
                .uri(String.format("%s/api/v1/listener/stop", apiDbUrl)).exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(OperationStatusDto.class).map(ResponseEntity::ok);
                    }
                    return response.bodyToMono(OperationStatusDto.class)
                            .map(body -> ResponseEntity
                                    .status(response.statusCode())
                                    .body(body));
                }).onErrorResume(WebClientResponseException.class, ex -> Mono.just(
                        ResponseEntity.status(ex.getStatusCode())
                                .body(ex.getResponseBodyAs(OperationStatusDto.class))))
                .onErrorResume(Exception.class, ex -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new OperationStatusDto(false, "Внутренняя ошибка: " + ex.getMessage()))));
    }

    @GetMapping("/is-running")
    public Mono<ResponseEntity<Boolean>> getIsRunning() {
        return client.get()
                .uri(String.format("%s/api/v1/listener/running", apiDbUrl)).exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(Boolean.class).map(ResponseEntity::ok);
                    }
                    return response.bodyToMono(Boolean.class)
                            .map(body -> ResponseEntity
                                    .status(response.statusCode())
                                    .body(body));
                })
                .onErrorResume(Exception.class, ex -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(false)));
    }
}
