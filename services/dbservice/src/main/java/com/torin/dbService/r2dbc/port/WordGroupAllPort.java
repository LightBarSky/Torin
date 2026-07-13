
package com.torin.dbService.r2dbc.port;

import java.time.Instant;

import com.torin.dbService.r2dbc.entity.WordGroupAll;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WordGroupAllPort {
        Mono<WordGroupAll> findByIdGroup(Long idGroup);

        Mono<WordGroupAll> save(WordGroupAll wordGroupAll);

        Flux<WordGroupAll> findByIdGroupIn(Long[] ids);

        Flux<WordGroupAll> findBatchByHandlerId(
                        Long handlersId,
                        Long offsetId,
                        Integer limit);

        Mono<WordGroupAll> findOneByHashGroupAndHandlersId(String hashGroup, Long handlersId);

        Mono<WordGroupAll> findOneByIdGroupAndHandlersId(Long idGroup, Long handlersId);

        Mono<Void> deleteById(Long id);

        Mono<WordGroupAll> findById(Long id);

        Mono<Integer> updateHandlersIdAndTotalSRAndTotalDP(
                        Long handlersId,
                        Long id,
                        Integer totalSendRequest,
                        Integer totalDetectPrivate);

        Mono<Integer> updateLastHandle(
                        Instant lastHandle,
                        Long id);
}