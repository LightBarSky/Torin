package com.torin.dbService.r2dbc.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import com.torin.dbService.r2dbc.entity.Handler;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface HandlerRepository extends R2dbcRepository<Handler, Long> {
    Flux<Handler> findAllByCategory(String category);

    Flux<Handler> findAllByOrderByIdAsc();

    @Query("""
                SELECT id
            FROM base_handler.handlers
            WHERE category = :category
            ORDER BY count_group ASC
            LIMIT 1
                """)
    Mono<Long> findIdByMinCountGroup(@Param("category") String category);

    @Modifying
    @Query("""
            UPDATE base_handler.handlers set
            count_group = base_handler.handlers.count_group + 1
            WHERE id = :id
                """)
    Mono<Integer> updateCountGroupByIdIncrement(@Param("id") Long id);

    @Modifying
    @Query("""
            UPDATE base_handler.handlers set
            count_group = base_handler.handlers.count_group - 1
            WHERE id = :id
            AND count_group > 0
                """)
    Mono<Integer> updateCountGroupByIdDecrement(@Param("id") Long id);
}
