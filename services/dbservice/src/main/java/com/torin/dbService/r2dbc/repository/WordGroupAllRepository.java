package com.torin.dbService.r2dbc.repository;

import java.time.Instant;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import com.torin.dbService.r2dbc.entity.WordGroupAll;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WordGroupAllRepository extends R2dbcRepository<WordGroupAll, Long> {
    Mono<WordGroupAll> findByIdGroup(Long idGroup);
    
    @Query("""
                SELECT *
                FROM base_group.word_group_all
                WHERE id_group = ANY(:ids)
            """)
    Flux<WordGroupAll> findByIdGroupIn(Long[] ids);

    @Query("""
            SELECT *
            FROM base_group.word_group_all
            WHERE handlers_id = :handlersId
            AND id > :offsetId
            ORDER BY id
            LIMIT :limit
            """)
    Flux<WordGroupAll> findBatchByHandlerId(
            Long handlersId,
            Long offsetId,
            Integer limit);

    Mono<WordGroupAll> findOneByHashGroupAndHandlersId(String hashGroup, Long handlersId);

    Mono<WordGroupAll> findOneByIdGroupAndHandlersId(Long idGroup, Long handlersId);

    Mono<Void> deleteById(Long id);

    Mono<WordGroupAll> findById(Long id);

    @Modifying
    @Query("""
                update base_group.word_group_all
                set handlers_id = :handlersId,
                    total_send_request = :totalSendRequest,
                    total_detect_private = :totalDetectPrivate
                where id = :id
            """)
    Mono<Integer> updateHandlersIdAndTotalSRAndTotalDP(
            @Param("handlersId") Long handlersId,
            @Param("id") Long id,
            @Param("totalSendRequest") Integer totalSendRequest,
            @Param("totalDetectPrivate") Integer totalDetectPrivate);

    @Modifying
    @Query("""
                update base_group.word_group_all
                set last_handle = :lastHandle
                where id = :id
            """)
    Mono<Integer> updateLastHandle(
            @Param("lastHandle") Instant lastHandle,
            @Param("id") Long id);
}
