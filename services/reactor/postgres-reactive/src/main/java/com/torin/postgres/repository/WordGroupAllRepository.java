package com.torin.postgres.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.torin.postgres.entity.WordGroupAll;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WordGroupAllRepository extends ReactiveCrudRepository<WordGroupAll, Long> {
    Mono<WordGroupAll> findByIdGroup(Long idGroup);

    @Query("""
                SELECT *
                FROM base_group.word_group_all
                WHERE LOWER(find_group) = LOWER(:findGroup)

            """)
    Flux<WordGroupAll> findByFindGroup(String findGroup);

    @Query("""
                SELECT *
                FROM base_group.word_group_all
                WHERE id_group = ANY(:ids)
            """)
    Flux<WordGroupAll> findByIdGroupIn(Long[] ids);
}
