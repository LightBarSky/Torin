package com.torin.dbService.elasticsearch.repository;

import java.util.Collection;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

import com.torin.dbService.elasticsearch.entity.LogHandler;

import reactor.core.publisher.Flux;

public interface LogHandlerRepository extends ReactiveElasticsearchRepository<LogHandler, String> {

    Flux<LogHandler> findByHandlerIdAndLevelInOrderByTimestampDesc(
            String handlerId,
            Collection<String> levels,
            Pageable pageable);
    
    Flux<LogHandler> findByLevelInOrderByTimestampDesc(
            Collection<String> levels,
            Pageable pageable);
}
