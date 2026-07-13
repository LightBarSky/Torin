package com.torin.postgres.adapter;

import com.torin.postgres.entity.WordGroupAll;
import com.torin.postgres.port.WordGroupAllPort;
import com.torin.postgres.repository.WordGroupAllRepository;

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
    public Flux<WordGroupAll> findByFindGroup(String findGroup) {
        return wordGroupAllRepository.findByFindGroup(findGroup);
    }

    @Override
    public Flux<WordGroupAll> findByIdGroupIn(Long[] ids) {
        return wordGroupAllRepository.findByIdGroupIn(ids);
    }

}
