package com.torin.prod.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.torin.prod.dto.InputSessionModalDto;
import com.torin.prod.dto.StartSessionModalDto;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/session")
public class ProxySessionController {
    @Value("${api.db.url}")
    private String apiDbUrl;
    @Value("${api.telegram.url}")
    private String apiTelegramUrl;
    @Autowired
    private WebClient client;

    @PostMapping("/start")
    public Mono<ResponseEntity<String>> postSessionStart(@ModelAttribute StartSessionModalDto startSessionModalDto) {
        return client.post()
                .uri(String.format("%s/api/v1/session/start", apiTelegramUrl))
                .bodyValue(startSessionModalDto)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class).map(ResponseEntity::ok);
                    }
                    return response.bodyToMono(String.class).defaultIfEmpty("empty")
                            .map(body -> ResponseEntity
                                    .status(response.statusCode())
                                    .body("Ошибка от telegramService: " + body));
                }).onErrorResume(WebClientResponseException.class, ex -> Mono.just(
                        ResponseEntity.status(ex.getStatusCode())
                                .body("Ошибка HTTP: " + ex.getResponseBodyAsString())))
                .onErrorResume(Exception.class, ex -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Внутренняя ошибка: " + ex.getMessage())));
    }

    @PostMapping("/input")
    public Mono<ResponseEntity<String>> postSessionInput(@ModelAttribute InputSessionModalDto inputSessionModalDto) {
        return client.post()
                .uri(String.format("%s/api/v1/session/input", apiTelegramUrl))
                .bodyValue(inputSessionModalDto)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class).map(ResponseEntity::ok);
                    }
                    return response.bodyToMono(String.class).defaultIfEmpty("empty")
                            .map(body -> ResponseEntity
                                    .status(response.statusCode())
                                    .body("Ошибка от telegramService: " + body));
                }).onErrorResume(WebClientResponseException.class, ex -> Mono.just(
                        ResponseEntity.status(ex.getStatusCode())
                                .body("Ошибка HTTP: " + ex.getResponseBodyAsString())))
                .onErrorResume(Exception.class, ex -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Внутренняя ошибка: " + ex.getMessage())));
    }

    @PostMapping("/stop")
    public Mono<ResponseEntity<Void>> getSessionStop() {
        return client.post()
                .uri(String.format("%s/api/v1/session/stop", apiTelegramUrl))
                .retrieve()
                .toBodilessEntity();
    }
}
