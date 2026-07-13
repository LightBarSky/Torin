package com.torin.prod.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.torin.prod.dto.HandlerDto;
import com.torin.prod.dto.HandlerStatus;
import com.torin.prod.dto.SessionsListDto;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
public class HomeService {
    @Value("${api.db.url}")
    private String apiUrl;
    @Value("${api.telegram.url}")
    private String apiTelegramUrl;
    @Value("${api.base.url}")
    private String apiBaseUrl;
    @Value("${key-for-logs-to-listener}")
    private String keyForLogsToListener;
    @Value("${services.nodes.dbService.key}")
    private String dbServiceKey;
    @Autowired
    private WebClient client;
    @Autowired
    private ServiceHealthChecker serviceHealthChecker;
    @Autowired
    private ServiceStatusHolder serviceStatusHolder;
    @Autowired
    private CacheService cacheService;

    public Mono<Tuple2<List<HandlerDto>, Integer>> getHandlers() {
        serviceHealthChecker.checkService();

        if (!serviceStatusHolder.GetAllStatuses().values().contains(false)) {
            Mono<SessionsListDto[]> sessionsListMono = client.get()
                    .uri(String.format("%s/api/v1/handlers/sessions-all", apiBaseUrl))
                    .retrieve()
                    .bodyToMono(SessionsListDto[].class);

            Mono<Long[]> runningMono = client.get()
                    .uri(String.format("%s/api/v1/handlers/running", apiTelegramUrl))
                    .retrieve()
                    .bodyToMono(Long[].class);
            return getHandlersAndUpdateCache().then(Mono.zip(sessionsListMono, runningMono)).map(tuple -> {
                List<HandlerDto> handlers = cacheService.getHandlers();
                Long[] running = tuple.getT2();
                SessionsListDto[] sessionsList = tuple.getT1();

                Set<Long> runningList = new HashSet<>(Arrays.asList(running));
                Map<String, SessionsListDto> sessionsMap = Arrays.stream(sessionsList)
                        .collect(Collectors.toMap(x -> x.getValue(), x -> x));
                for (HandlerDto handler : handlers) {
                    if (runningList.contains(handler.getId())) {
                        handler.setStatus(HandlerStatus.Running);
                    } else {
                        handler.setStatus(HandlerStatus.Stopped);
                    }

                    if (sessionsMap.containsKey(handler.getPhone())) {
                        List<Long> handlersId = sessionsMap.get(handler.getPhone()).getHandlersId();
                        if (handlersId != null && handlersId.size() > 1) {
                            handler.setWarning("The selected session is already in use: " + handlersId
                                    .stream().map(String::valueOf).collect(Collectors.joining(",")));
                        }
                    }
                }
                return Tuples.of(handlers, running.length);
            });
        }
        return Mono.just(Tuples.of(new ArrayList<HandlerDto>(), 0));
    }

    public Mono<Boolean> getRunningListener() {
        if (serviceStatusHolder.isAvailability(dbServiceKey)) {
            return client.get()
                    .uri(String.format("%s/api/v1/listener/running", apiUrl))
                    .retrieve()
                    .bodyToMono(Boolean.class).defaultIfEmpty(false);
        }
        return Mono.just(false);
    }

    private Mono<HandlerDto[]> getHandlersAndUpdateCache() {
        return client.get()
                .uri(String.format("%s/api/v1/handlers", apiUrl))
                .retrieve()
                .bodyToMono(HandlerDto[].class).map(hands -> {
                    cacheService.updateHandlers(Arrays.asList(hands));
                    return hands;
                });
    }
}
