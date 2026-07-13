package com.torin.dbService.r2dbc.service;

import java.time.Instant;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.torin.dbService.dto.WordGroupAllDto;
import com.torin.dbService.r2dbc.port.WordGroupAllPort;
import com.torin.dbService.service.MapperService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WordGroupAllService {

    private final WordGroupAllPort wordGroupAllPort;

    private final HandlerService handlerService;

    private final MapperService mapperService;

    public WordGroupAllService(WordGroupAllPort wordGroupAllPort, HandlerService handlerService,
            MapperService mapperService) {
        this.wordGroupAllPort = wordGroupAllPort;
        this.handlerService = handlerService;
        this.mapperService = mapperService;
    }

    public Mono<WordGroupAllDto> findByIdGroup(Long idGroup) {
        return wordGroupAllPort.findByIdGroup(idGroup)
                .map(wordGr -> mapperService.toDto(wordGr));
    }

    public Flux<WordGroupAllDto> findBatchByHandlerId(Long handlersId, Long offsetId, Integer limit) {
        limit = Math.max(10, Math.min(limit, 100));
        return wordGroupAllPort.findBatchByHandlerId(handlersId, offsetId, limit)
                .map(wordGr -> mapperService.toDto(wordGr));
    }

    public Mono<WordGroupAllDto> findByIdGroupAndHandlersId(Long idGroup,
            Long handlersId) {
        return wordGroupAllPort.findOneByIdGroupAndHandlersId(idGroup, handlersId)
                .map(wordGr -> mapperService.toDto(wordGr));
    }

    public Mono<WordGroupAllDto> findById(Long id) {
        return wordGroupAllPort.findById(id).map(wordGr -> mapperService.toDto(wordGr));
    }

    public Mono<WordGroupAllDto> findByHashGroupAndHandlersId(String hashGroup,
            Long handlersId) {
        return wordGroupAllPort.findOneByHashGroupAndHandlersId(hashGroup, handlersId)
                .map(wordGr -> mapperService.toDto(wordGr));
    }

    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<WordGroupAllDto> addWordGroupAll(WordGroupAllDto wordGroupAllDto) {
        // method used in GetFullChat TelegramService:ParseGroup
        // проверка на idGroup is null не нужна, так как изначально делаю запрос на
        // наличие группы для обновления старых данных и записи изменений
        if (wordGroupAllDto.getId() != null) {
            return Mono.error(new IllegalArgumentException("ID must be null for new entity"));
        }
        return handlerService
                .updateCountGroupByIdIncrement(wordGroupAllDto.getHandlersId())
                .then(wordGroupAllPort.save(mapperService.toEntity(wordGroupAllDto)))
                .map(mapperService::toDto);
    }

    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<WordGroupAllDto> addWordGroupAllNew(WordGroupAllDto wordGroupAllDto) {
        if (wordGroupAllDto.getId() != null) {
            return Mono.error(new IllegalArgumentException("ID must be null for new entity"));
        }
        if (wordGroupAllDto.getIdGroup() != null) {
            return Mono.error(new IllegalArgumentException("idGroup must be null for new entity"));
        }
        if ((wordGroupAllDto.getFindGroup() == null || wordGroupAllDto.getFindGroup().equals("")) &&
                (wordGroupAllDto.getHashGroup() == null || wordGroupAllDto.getHashGroup().equals(""))) {
            return Mono.error(new IllegalArgumentException("FindGroup or HashGroup must be null for new entity"));
        }

        return handlerService.findIdByMinCountGroupParseGroup()
                .switchIfEmpty(Mono.error(new RuntimeException("No handlers found")))
                .flatMap(idHandlers -> {

                    var entity = mapperService.toEntity(wordGroupAllDto);
                    entity.setHandlersId(idHandlers);

                    return handlerService.updateCountGroupByIdIncrement(idHandlers)
                            .flatMap(res -> wordGroupAllPort.save(entity))
                            .map(mapperService::toDto);
                });
    }

    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<WordGroupAllDto> updateWordGroupAll(Long id, WordGroupAllDto wordGroupAllDto) {
        if (!Objects.equals(id, wordGroupAllDto.getId())) {
            return Mono.error(new IllegalArgumentException("Id mismatch"));
        }
        return wordGroupAllPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("WordGroupAll:update not found")))
                .flatMap(e -> {

                    return recalculateHandlerCountGroup(e.getHandlersId(), wordGroupAllDto.getHandlersId())
                            .then(wordGroupAllPort.save(mapperService.toEntity(wordGroupAllDto)));
                })
                .map(mapperService::toDto);
    }

    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<Boolean> updateWGAHandlersIdAndTotalSRAndTotalDP(Long id, WordGroupAllDto wordGroupAllDto) {

        return wordGroupAllPort.findById(id)
                .flatMap(e -> {
                    return recalculateHandlerCountGroup(e.getHandlersId(), wordGroupAllDto.getHandlersId())
                            .then(
                                    wordGroupAllPort.updateHandlersIdAndTotalSRAndTotalDP(
                                            wordGroupAllDto.getHandlersId(),
                                            id,
                                            wordGroupAllDto.getTotalSendRequest(),
                                            wordGroupAllDto.getTotalDetectPrivate()).map(i -> i > 0));
                });
    }

    public Mono<Boolean> updateWGALastHandle(Long id, Instant lastHandle) {
        if (lastHandle == null) {
            return Mono.error(new IllegalArgumentException("LastHandle of type Instant not must be is null!"));
        }
        return wordGroupAllPort
                .updateLastHandle(lastHandle, id).map(i -> i > 0);
    }

    public Mono<Void> deleteById(Long id) {
        return wordGroupAllPort.deleteById(id);
    }

    private Mono<Void> recalculateHandlerCountGroup(Long oldHandler, Long newHandler) {
        Mono<Integer> decrementMono = Mono.empty();
        Mono<Integer> incrementMono = Mono.empty();

        if (newHandler == -1) {
            decrementMono = handlerService
                    .updateCountGroupByIdDecrement(oldHandler);
        } else if (!Objects.equals(newHandler, oldHandler)) {
            decrementMono = handlerService
                    .updateCountGroupByIdDecrement(oldHandler);

            incrementMono = handlerService
                    .updateCountGroupByIdIncrement(newHandler);
        }
        return Mono.when(decrementMono, incrementMono);
    }

}
