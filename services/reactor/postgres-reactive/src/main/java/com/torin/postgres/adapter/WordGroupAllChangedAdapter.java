package com.torin.postgres.adapter;

import java.time.Instant;

import com.torin.postgres.entity.WordGroupAllChanged;
import com.torin.postgres.port.WordGroupAllChangedPort;
import com.torin.postgres.repository.WordGroupAllChangedRepository;

import reactor.core.publisher.Flux;

public class WordGroupAllChangedAdapter implements WordGroupAllChangedPort {
    private final WordGroupAllChangedRepository wordGroupAllChangedRepository;

    public WordGroupAllChangedAdapter(WordGroupAllChangedRepository wordGroupAllChangedRepository) {
        this.wordGroupAllChangedRepository = wordGroupAllChangedRepository;
    }

    @Override
    public Flux<WordGroupAllChanged> findAllByIdGroup(Long idGroup) {
        return wordGroupAllChangedRepository.findAllByIdGroup(idGroup);
    }

    @Override
    public Flux<WordGroupAllChanged> findAllByIdGroupAndDateBetween(Long idGroup, Instant from, Instant to) {
        return wordGroupAllChangedRepository.findAllByIdGroupAndDateBetween(idGroup, from, to);
    }
    
}
