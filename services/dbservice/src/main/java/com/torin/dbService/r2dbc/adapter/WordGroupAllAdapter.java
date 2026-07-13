package com.torin.dbService.r2dbc.adapter;

import java.time.Instant;

import com.torin.dbService.r2dbc.entity.WordGroupAll;
import com.torin.dbService.r2dbc.port.WordGroupAllPort;
import com.torin.dbService.r2dbc.repository.WordGroupAllRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class WordGroupAllAdapter implements WordGroupAllPort {

    private final WordGroupAllRepository wordGroupAllRepository;

    public WordGroupAllAdapter(WordGroupAllRepository wordGroupAllRepository) {
        this.wordGroupAllRepository = wordGroupAllRepository;
    }

    @Override
    public Mono<WordGroupAll> findByIdGroup(Long idGroup) {
        return wordGroupAllRepository.findByIdGroup(idGroup);
    }

    @Override
    public Flux<WordGroupAll> findByIdGroupIn(Long[] ids) {
        return wordGroupAllRepository.findByIdGroupIn(ids);
    }

    @Override
    public Flux<WordGroupAll> findBatchByHandlerId(Long handlersId, Long offsetId, Integer limit) {
        return wordGroupAllRepository.findBatchByHandlerId(handlersId, offsetId, limit);
    }

    @Override
    public Mono<WordGroupAll> findOneByHashGroupAndHandlersId(String hashGroup, Long handlersId) {
        return wordGroupAllRepository.findOneByHashGroupAndHandlersId(hashGroup, handlersId);
    }

    @Override
    public Mono<WordGroupAll> findOneByIdGroupAndHandlersId(Long idGroup, Long handlersId) {
        return wordGroupAllRepository.findOneByIdGroupAndHandlersId(idGroup, handlersId);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return wordGroupAllRepository.deleteById(id);
    }

    @Override
    public Mono<WordGroupAll> findById(Long id) {
        return wordGroupAllRepository.findById(id);
    }

    @Override
    public Mono<Integer> updateHandlersIdAndTotalSRAndTotalDP(Long handlersId, Long id, Integer totalSendRequest,
            Integer totalDetectPrivate) {
        return wordGroupAllRepository.updateHandlersIdAndTotalSRAndTotalDP(handlersId, id, totalSendRequest,
                totalDetectPrivate);
    }

    @Override
    public Mono<Integer> updateLastHandle(Instant lastHandle, Long id) {
        return wordGroupAllRepository.updateLastHandle(lastHandle, id);
    }

    @Override
    public Mono<WordGroupAll> save(WordGroupAll wordGroupAll) {
        return wordGroupAllRepository.save(wordGroupAll);
    }

}
