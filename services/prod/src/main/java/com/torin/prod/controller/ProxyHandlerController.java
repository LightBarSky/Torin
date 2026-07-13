package com.torin.prod.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.torin.prod.dto.HandlerDto;
import com.torin.prod.dto.SessionsListDto;
import com.torin.prod.service.CacheService;
import com.torin.prod.service.RunAllHandlerService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/handlers")
public class ProxyHandlerController {
    @Value("${api.db.url}")
    private String apiDbUrl;
    @Value("${api.telegram.url}")
    private String apiTelegramUrl;
    @Autowired
    private WebClient client;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RunAllHandlerService runAllHandlerService;

    @GetMapping("/{id}")
    public Mono<ResponseEntity<HandlerDto>> getHandler(@PathVariable Long id) {
        return client.get()
                .uri(String.format("%s/api/v1/handlers/%s", apiDbUrl, id))
                .retrieve()
                .bodyToMono(HandlerDto.class).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteHandler(@PathVariable Long id) {
        return client.delete()
                .uri(String.format("%s/api/v1/handlers/%s", apiDbUrl, id))
                .retrieve()
                .bodyToMono(Void.class).map(x -> ResponseEntity.noContent().build());
    }

    @GetMapping("/sessions-all")
    public Mono<ResponseEntity<SessionsListDto[]>> getSessions(@RequestParam(required = false) Long handlerId) {
        return client.get()
                .uri(String.format("%s/api/v1/handlers/sessions-all", apiTelegramUrl))
                .retrieve()
                .bodyToMono(SessionsListDto[].class).map(arr -> {
                    List<HandlerDto> handlers = cacheService.getHandlers();
                    Map<String, SessionsListDto> sessions = Arrays.stream(arr)
                            .collect(Collectors.toMap(SessionsListDto::getValue, dto -> dto, (oldVal, newVal) -> newVal,
                                    HashMap::new));

                    for (HandlerDto handler : handlers) {
                        if (sessions.containsKey(handler.getPhone())) {
                            if (handlerId != null && Objects.equals(handlerId, handler.getId())) {
                                continue;
                            }
                            sessions.computeIfPresent(handler.getPhone(), (key, val) -> {
                                if (val.getHandlersId() == null) {
                                    val.setHandlersId(new ArrayList<>());
                                }
                                val.getHandlersId().add(handler.getId());
                                return val;
                            });
                        }
                    }
                    return ResponseEntity.ok(sessions.values().toArray(SessionsListDto[]::new));
                }).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<HandlerDto>> postHandler(@RequestBody HandlerDto handlerDto) {
        return client.post()
                .uri(String.format("%s/api/v1/handlers", apiDbUrl))
                .bodyValue(handlerDto)
                .retrieve()
                .bodyToMono(HandlerDto.class).map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<HandlerDto>> putHandler(@PathVariable Long id, @RequestBody HandlerDto handlerDto) {
        if (!handlerDto.getId().equals(id)) {
            return Mono.error(new IllegalArgumentException("Id of entity and Id mismatch"));
        }
        return client.put()
                .uri(String.format("%s/api/v1/handlers/%s", apiDbUrl, id))
                .bodyValue(handlerDto)
                .retrieve()
                .bodyToMono(HandlerDto.class).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/start")
    public Mono<ResponseEntity<String>> getHandlersStart(@PathVariable Long id) {
        return client.post()
                .uri(String.format("%s/api/v1/handlers/%s/start", apiTelegramUrl, id))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class).map(ResponseEntity::ok);
                    }
                    return response.bodyToMono(String.class).defaultIfEmpty("empty")
                            .map(body -> ResponseEntity
                                    .status(response.statusCode())
                                    .body(body));
                }).onErrorResume(WebClientResponseException.class, ex -> Mono.just(
                        ResponseEntity.status(ex.getStatusCode())
                                .body("Ошибка HTTP: " + ex.getResponseBodyAsString())))
                .onErrorResume(Exception.class, ex -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Внутренняя ошибка: " + ex.getMessage())));
    }

    @PostMapping("/{id}/stop")
    public Mono<ResponseEntity<String>> getHandlersStop(@PathVariable Long id) {
        return client.post()
                .uri(String.format("%s/api/v1/handlers/%s/stop", apiTelegramUrl, id))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class).map(ResponseEntity::ok);
                    }
                    return response.bodyToMono(String.class).defaultIfEmpty("empty")
                            .map(body -> ResponseEntity
                                    .status(response.statusCode())
                                    .body(body));
                }).onErrorResume(WebClientResponseException.class, ex -> Mono.just(
                        ResponseEntity.status(ex.getStatusCode())
                                .body("Ошибка HTTP: " + ex.getResponseBodyAsString())))
                .onErrorResume(Exception.class, ex -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Внутренняя ошибка: " + ex.getMessage())));
    }

    @PostMapping("/run-all")
    public void runAllHandler() {
        runAllHandlerService.start();
    }

    @PostMapping("/stop-all")
    public void stopAllHandler() {
        runAllHandlerService.stop();
    }
}
