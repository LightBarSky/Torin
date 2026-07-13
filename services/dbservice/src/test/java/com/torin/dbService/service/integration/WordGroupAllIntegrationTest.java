package com.torin.dbService.service.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.torin.dbService.dto.WordGroupAllDto;
import com.torin.dbService.r2dbc.entity.Handler;
import com.torin.dbService.r2dbc.entity.WordGroupAll;
import com.torin.dbService.r2dbc.repository.HandlerRepository;
import com.torin.dbService.r2dbc.repository.WordGroupAllRepository;
import com.torin.dbService.r2dbc.service.WordGroupAllService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class WordGroupAllIntegrationTest extends AbstractIntegrationDBTest {
        @Autowired
        private WordGroupAllService wordGroupAllService;

        @Autowired
        @MockitoSpyBean
        private WordGroupAllRepository wordGroupAllRepository;

        @Autowired
        private HandlerRepository handlerRepository;

        @DynamicPropertySource
        static void configure(DynamicPropertyRegistry registry) {
                registry.add("spring.flyway.locations", () -> "classpath:db/test-migration/wordGroupAll");
        }

        @Test
        void findByIdGroupTest() {
                WordGroupAll wordGroupAll = new WordGroupAll(null, 111L, "info1", "title1", "username1",
                                null, 0L, 1, 111L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                StepVerifier.create(wordGroupAllRepository.save(wordGroupAll)
                                .flatMap(saved -> wordGroupAllService.findByIdGroup(saved.getIdGroup())))
                                .expectNextCount(1).verifyComplete();
        }

        @Test
        void findBatchByHandlerId() {
                long handlersId = 222L;
                List<WordGroupAll> list = List.of(
                                new WordGroupAll(null, 222L, "info1", "title1", "username1",
                                                null, 0L, 1, handlersId,
                                                Instant.now(), Instant.now(), 0, 0,
                                                0L, 123L, Instant.now(), null, null),
                                new WordGroupAll(null, 333L, "info14", "title14", "username14",
                                                null, 0L, 0, handlersId,
                                                Instant.now(), Instant.now(), 0, 0,
                                                0L, 123L, Instant.now(), null, null));

                StepVerifier.create(wordGroupAllRepository.saveAll(list).reduce((a, b) -> a.getId() < b.getId() ? a : b)
                                .flatMapMany(a -> wordGroupAllService.findBatchByHandlerId(handlersId, a.getId(), 1)))
                                .expectNextCount(1).verifyComplete();
        }

        @Test
        void findByIdGroupAndHandlersIdTest() {
                WordGroupAll wordGroupAll = new WordGroupAll(null, 444L, "info1", "title1", "username1",
                                null, 0L, 1, 444L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                StepVerifier.create(wordGroupAllRepository.save(wordGroupAll)
                                .flatMap(saved -> wordGroupAllService.findByIdGroupAndHandlersId(saved.getIdGroup(),
                                                saved.getHandlersId())))
                                .expectNextCount(1).verifyComplete();
        }

        @Test
        void findByIdTest() {
                WordGroupAll wordGroupAll = new WordGroupAll(null, 555L, "info1", "title1", "username1",
                                null, 0L, 1, 555L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                StepVerifier.create(wordGroupAllRepository.save(wordGroupAll)
                                .flatMap(saved -> wordGroupAllService.findById(saved.getId()))).assertNext(wg -> {
                                        assertEquals(wg.getIdGroup(), wordGroupAll.getIdGroup());
                                }).verifyComplete();
        }

        @Test
        void findByHashGroupAndHandlersIdTest() {
                WordGroupAll wordGroupAll = new WordGroupAll(null, 666L, "info1", "title1", null,
                                "12345", 0L, 1, 666L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                StepVerifier.create(wordGroupAllRepository.save(wordGroupAll)
                                .flatMap(saved -> wordGroupAllService.findByHashGroupAndHandlersId(saved.getHashGroup(),
                                                saved.getHandlersId())))
                                .assertNext(wg -> {
                                        assertEquals(wg.getIdGroup(), wordGroupAll.getIdGroup());
                                }).verifyComplete();
        }

        @Test
        void addWordGroupAllWithIdShouldFail() {
                WordGroupAllDto dto = new WordGroupAllDto(1L, 4321L, "info4", "title4", "username4",
                                null, 0L, 1, 1L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);

                StepVerifier.create(wordGroupAllService.addWordGroupAll(dto))
                                .expectErrorMatches(trowable -> trowable instanceof IllegalArgumentException &&
                                                trowable.getMessage().equals("ID must be null for new entity"))
                                .verify();
        }

        @Test
        void addWordGroupAllTest() {
                WordGroupAllDto dto = new WordGroupAllDto(null, 777L, "info4", "title4", "username4",
                                null, 0L, 1, null,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                Handler handler = new Handler(null, 52L, "hash", "79991", "", "",
                                "ParseGroup", 0, "OneHandler");

                StepVerifier.create(handlerRepository.save(handler).flatMap(saved -> {
                        dto.setHandlersId(saved.getId());
                        return wordGroupAllService.addWordGroupAll(dto)
                                        .flatMap(wg -> handlerRepository.findById(saved.getId())
                                                        .map(hand -> Tuples.of(wg, hand)));
                }))
                                .assertNext(tuple -> {
                                        assertNotNull(tuple.getT1().getId());
                                        assertEquals(tuple.getT1().getIdGroup(), dto.getIdGroup());
                                        assertEquals(tuple.getT2().getCountGroup(), handler.getCountGroup() + 1);
                                }).verifyComplete();
        }

        @Test
        void addWordGroupAllRollbackTransaction() {
                WordGroupAll wordGroupAll = new WordGroupAll(null, 888L, "info2", "title2", "username2",
                                null, 0L, 1, 888L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                WordGroupAllDto dto = new WordGroupAllDto(null, 888L, "info4", "title4", "username4",
                                null, 0L, 1, 888L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                Handler handler = new Handler(null, 52L, "hash", "79991", "", "",
                                "ParseGroup", 0, "OneHandler");

                StepVerifier.create(handlerRepository.save(handler).flatMap(saved -> {
                        wordGroupAll.setHandlersId(saved.getId());
                        dto.setHandlersId(saved.getId());
                        handler.setId(saved.getId());
                        return wordGroupAllRepository.save(wordGroupAll).then(wordGroupAllService.addWordGroupAll(dto));
                })
                                .onErrorReturn(dto)
                                .flatMap(wg -> handlerRepository.findById(handler.getId())
                                                .zipWith(wordGroupAllRepository
                                                                .findByIdGroup(wordGroupAll.getIdGroup()))
                                                .map(tuple -> Tuples.of(tuple.getT1(), tuple.getT2(), wg))))
                                .assertNext(tuple -> {
                                        assertNotEquals(tuple.getT2().getFindGroup(), dto.getFindGroup());
                                        assertEquals(tuple.getT1().getCountGroup(), handler.getCountGroup());
                                        assertNotNull(tuple.getT3());
                                }).verifyComplete();
        }

        @Test
        void addWordGroupAllNewWithIdShouldFail() {
                WordGroupAllDto dto = new WordGroupAllDto(1L, 4321L, "info4", "title4", "username4",
                                null, 0L, 1, 1L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                StepVerifier.create(wordGroupAllService.addWordGroupAllNew(dto))
                                .expectErrorMatches(trowable -> trowable instanceof IllegalArgumentException &&
                                                trowable.getMessage().equals("ID must be null for new entity"))
                                .verify();
        }

        @Test
        void addWordGroupAllNewWithIdGroupShouldFail() {
                WordGroupAllDto dto = new WordGroupAllDto(null, 4321L, null, null, "username4",
                                null, 0L, 1, 1L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                StepVerifier.create(wordGroupAllService.addWordGroupAllNew(dto))
                                .expectErrorMatches(trowable -> trowable instanceof IllegalArgumentException &&
                                                trowable.getMessage().equals("idGroup must be null for new entity"))
                                .verify();
        }

        @Test
        void addWordGroupAllNewWithFGAndHGIsNullShouldFail() {
                WordGroupAllDto dto = new WordGroupAllDto(null, null, null, null, null,
                                null, 0L, 1, 1L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                StepVerifier.create(wordGroupAllService.addWordGroupAllNew(dto))
                                .expectErrorMatches(trowable -> trowable instanceof IllegalArgumentException &&
                                                trowable.getMessage().equals(
                                                                "FindGroup or HashGroup must be null for new entity"))
                                .verify();
        }

        @Test
        void addWordGroupAllNewTest() {
                WordGroupAllDto dto = new WordGroupAllDto(null, null, null, null, "username4",
                                null, 0L, 1, null,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                List<Handler> handlerList = List.of(new Handler(null, 52L, "hash", "79991", "", "",
                                "ParseGroup", 0, "OneHandler"),
                                new Handler(null, 53L, "hash", "79992", "", "",
                                                "ParseGroup", 5, "TwoHandler"));
                StepVerifier.create(
                                handlerRepository.saveAll(handlerList).then(wordGroupAllService.addWordGroupAllNew(dto))
                                                .flatMap(res -> handlerRepository.findById(res.getHandlersId())
                                                                .map(hand -> Tuples.of(res, hand))))
                                .assertNext(tuple -> {
                                        assertNotNull(tuple.getT1().getId());
                                        assertNotNull(tuple.getT1().getHandlersId());
                                        assertEquals(tuple.getT2().getCountGroup(), handlerList.stream()
                                                        .min((a, b) -> a.getCountGroup() - b.getCountGroup()).get()
                                                        .getCountGroup() + 1);
                                }).verifyComplete();
        }

        @Test
        void addWordGroupAllNewWithEmptyHandlersShouldFail() {
                WordGroupAllDto dto = new WordGroupAllDto(null, null, null, null, "username4",
                                null, 0L, 1, null,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                handlerRepository.deleteAll().block();
                StepVerifier.create(wordGroupAllService.addWordGroupAllNew(dto))
                                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                                                && throwable.getMessage().equals("No handlers found"))
                                .verify();
        }

        @Test
        void addWordGroupAllNewRollbackTransaction() {
                WordGroupAllDto dto = new WordGroupAllDto(null, null, null, null, "username4",
                                null, 0L, 1, null,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                Handler handler1 = new Handler(null, 522L, "hash", "79991", "", "",
                                "ParseGroup", 0, "OneHandler");
                Handler handler2 = new Handler(null, 533L, "hash", "79992", "", "",
                                "ParseGroup", 5, "TwoHandler");
                int minCountGroup = Math.min(handler1.getCountGroup(), handler2.getCountGroup());

                doReturn(Mono.error(new RuntimeException()))
                                .when(wordGroupAllRepository)
                                .save(any());

                StepVerifier.create(
                                handlerRepository.saveAll(List.of(handler1, handler2))
                                                .reduce((a, b) -> a.getCountGroup() < b.getCountGroup() ? a : b)
                                                .flatMap(h -> {
                                                        return wordGroupAllService.addWordGroupAllNew(dto)
                                                                        .onErrorReturn(dto).flatMap(d -> Mono.just(
                                                                                        Tuples.of(dto, h.getId())));
                                                })
                                                .flatMap(res -> handlerRepository.findById(res.getT2())
                                                                .map(hand -> Tuples.of(res.getT1(), hand))))
                                .assertNext(tuple -> {
                                        assertNull(tuple.getT1().getId());
                                        assertNull(tuple.getT1().getHandlersId());
                                        assertEquals(tuple.getT2().getCountGroup(), minCountGroup);
                                }).verifyComplete();
        }

        @Test
        void updateWordGroupAllTest() {
                WordGroupAll wordGroupAll = new WordGroupAll(null, 999L, "info2", "title2", "username2",
                                null, 0L, 1, null,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                WordGroupAllDto dto = new WordGroupAllDto(null, 999L, null, null, "username4",
                                null, 0L, 1, null,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                Handler handlerBefore = new Handler(null, 52L, "hash", "79991", "", "",
                                "ParseGroup", 0, "OneHandler");
                Handler handlerAfter = new Handler(null, 53L, "hash", "79992", "", "",
                                "ParseGroup", 5, "TwoHandler");

                StepVerifier.create(handlerRepository.saveAll(List.of(handlerBefore, handlerAfter))
                                .collect(Collectors.toMap(x -> x.getApiId(), h -> h)).flatMap(list -> {
                                        handlerBefore.setId(list.get(handlerBefore.getApiId()).getId());
                                        handlerAfter.setId(list.get(handlerAfter.getApiId()).getId());
                                        wordGroupAll.setHandlersId(handlerBefore.getId());
                                        dto.setHandlersId(handlerAfter.getId());
                                        return wordGroupAllRepository.save(wordGroupAll).flatMap(saved -> {
                                                dto.setId(saved.getId());
                                                return wordGroupAllService.updateWordGroupAll(dto.getId(), dto);
                                        });
                                })
                                .flatMap(wg -> handlerRepository.findById(handlerBefore.getId())
                                                .zipWith(handlerRepository.findById(handlerAfter.getId()))
                                                .map(hand -> Tuples.of(wg, hand.getT1(), hand.getT2()))))
                                .assertNext(tuple -> {
                                        Handler handlerBeforeAs = tuple.getT2();
                                        Handler handlerAfterAs = tuple.getT3();
                                        assertEquals(tuple.getT1().getHandlersId(), handlerAfter.getId());
                                        assertEquals(tuple.getT1().getFindGroup(), "username4");
                                        assertEquals(handlerAfterAs.getCountGroup(), handlerAfter.getCountGroup() + 1);
                                        assertEquals(handlerBeforeAs.getCountGroup(),
                                                        Math.max(handlerBefore.getCountGroup() - 1, 0));
                                }).verifyComplete();
        }

        @Test
        void updateWordGroupAllIdMismatch() {
                WordGroupAllDto dto = new WordGroupAllDto(1L, 123L, null, null, "username4",
                                null, 0L, 1, 2L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                StepVerifier.create(wordGroupAllService.updateWordGroupAll(2L, dto))
                                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                                                && throwable.getMessage().equals("Id mismatch"))
                                .verify();
        }

        @Test
        void updateWordGroupAllRollbackTransaction() {
                WordGroupAll wordGroupAll = new WordGroupAll(null, 9991L, "info2", "title2", "username2",
                                null, 0L, 1, null,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                WordGroupAllDto dto = new WordGroupAllDto(null, 9991L, null, null, "username4",
                                null, 0L, 1, null,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                Handler handlerBefore = new Handler(null, 52L, "hash", "79991", "", "",
                                "ParseGroup", 0, "OneHandler");
                Handler handlerAfter = new Handler(null, 53L, "hash", "79992", "", "",
                                "ParseGroup", 5, "TwoHandler");

                doReturn(Mono.error(new RuntimeException()))
                                .when(wordGroupAllRepository)
                                .save(any());

                StepVerifier.create(handlerRepository.saveAll(List.of(handlerBefore, handlerAfter))
                                .collect(Collectors.toMap(x -> x.getApiId(), h -> h)).flatMap(list -> {
                                        handlerBefore.setId(list.get(handlerBefore.getApiId()).getId());
                                        handlerAfter.setId(list.get(handlerAfter.getApiId()).getId());
                                        wordGroupAll.setHandlersId(handlerBefore.getId());
                                        dto.setHandlersId(handlerAfter.getId());
                                        return wordGroupAllRepository.save(wordGroupAll).flatMap(saved -> {
                                                dto.setId(saved.getId());
                                                return wordGroupAllService.updateWordGroupAll(dto.getId(), dto);
                                        });
                                })
                                .onErrorReturn(dto)
                                .flatMap(wg -> handlerRepository.findById(handlerBefore.getId())
                                                .zipWith(handlerRepository.findById(handlerAfter.getId()))
                                                .map(hand -> Tuples.of(wg, hand.getT1(), hand.getT2()))))
                                .assertNext(tuple -> {
                                        Handler handlerBeforeAs = tuple.getT2();
                                        Handler handlerAfterAs = tuple.getT3();
                                        assertEquals(handlerAfterAs.getCountGroup(), handlerAfter.getCountGroup());
                                        assertEquals(handlerBeforeAs.getCountGroup(),
                                                        handlerBefore.getCountGroup());
                                }).verifyComplete();
        }

        @Test
        void updateWGAHandlersIdAndTotalSRAndTotalDPTest() {
                WordGroupAll wordGroupAll = new WordGroupAll(null, 99911L, "info2", "title2", "username2",
                                null, 0L, 1, null,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                WordGroupAllDto dto = new WordGroupAllDto(null, 99911L, null, null, "username4",
                                null, 0L, 1, null,
                                Instant.now(), Instant.now(), 5, 10,
                                0L, 123L, Instant.now(), null, null);
                Handler handlerBefore = new Handler(null, 52L, "hash", "79991", "", "",
                                "ParseGroup", 0, "OneHandler");
                Handler handlerAfter = new Handler(null, 53L, "hash", "79992", "", "",
                                "ParseGroup", 5, "TwoHandler");

                StepVerifier.create(handlerRepository.saveAll(List.of(handlerBefore, handlerAfter))
                                .collect(Collectors.toMap(x -> x.getApiId(), h -> h)).flatMap(list -> {
                                        handlerBefore.setId(list.get(handlerBefore.getApiId()).getId());
                                        handlerAfter.setId(list.get(handlerAfter.getApiId()).getId());
                                        wordGroupAll.setHandlersId(handlerBefore.getId());
                                        dto.setHandlersId(handlerAfter.getId());
                                        return wordGroupAllRepository.save(wordGroupAll).flatMap(saved -> {
                                                dto.setId(saved.getId());
                                                return wordGroupAllService.updateWGAHandlersIdAndTotalSRAndTotalDP(
                                                                dto.getId(), dto);
                                        });
                                })
                                .flatMap(wg -> handlerRepository.findById(handlerBefore.getId())
                                                .zipWith(handlerRepository.findById(handlerAfter.getId()))
                                                .zipWith(wordGroupAllRepository
                                                                .findByIdGroup(wordGroupAll.getIdGroup()))))
                                .assertNext(tuple -> {
                                        Handler handlerBeforeAs = tuple.getT1().getT1();
                                        Handler handlerAfterAs = tuple.getT1().getT2();
                                        assertEquals(tuple.getT2().getHandlersId(), handlerAfter.getId());
                                        assertEquals(tuple.getT2().getTotalSendRequest(), dto.getTotalSendRequest());
                                        assertEquals(tuple.getT2().getTotalDetectPrivate(),
                                                        dto.getTotalDetectPrivate());
                                        assertEquals(handlerAfterAs.getCountGroup(), handlerAfter.getCountGroup() + 1);
                                        assertEquals(handlerBeforeAs.getCountGroup(),
                                                        Math.max(handlerBefore.getCountGroup() - 1, 0));
                                }).verifyComplete();
        }

        @Test
        void updateWGAHandlersIdAndTotalSRAndTotalDPRollback() {
                WordGroupAll wordGroupAll = new WordGroupAll(null, 999112L, "info2", "title2", "username2",
                                null, 0L, 1, null,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                WordGroupAllDto dto = new WordGroupAllDto(null, 999112L, null, null, "username4",
                                null, 0L, 1, null,
                                Instant.now(), Instant.now(), 5, 10,
                                0L, 123L, Instant.now(), null, null);
                Handler handlerBefore = new Handler(null, 52L, "hash", "79991", "", "",
                                "ParseGroup", 0, "OneHandler");
                Handler handlerAfter = new Handler(null, 53L, "hash", "79992", "", "",
                                "ParseGroup", 5, "TwoHandler");

                doReturn(Mono.error(new RuntimeException("boom")))
                                .when(wordGroupAllRepository)
                                .updateHandlersIdAndTotalSRAndTotalDP(any(), any(), any(), any());

                StepVerifier.create(handlerRepository.saveAll(List.of(handlerBefore, handlerAfter))
                                .collect(Collectors.toMap(x -> x.getApiId(), h -> h)).flatMap(list -> {
                                        handlerBefore.setId(list.get(handlerBefore.getApiId()).getId());
                                        handlerAfter.setId(list.get(handlerAfter.getApiId()).getId());
                                        wordGroupAll.setHandlersId(handlerBefore.getId());
                                        dto.setHandlersId(handlerAfter.getId());
                                        return wordGroupAllRepository.save(wordGroupAll).flatMap(saved -> {
                                                dto.setId(saved.getId());
                                                return wordGroupAllService.updateWGAHandlersIdAndTotalSRAndTotalDP(
                                                                dto.getId(), dto).onErrorReturn(false);
                                        });
                                })
                                .flatMap(wg -> handlerRepository.findById(handlerBefore.getId())
                                                .zipWith(handlerRepository.findById(handlerAfter.getId()))
                                                .zipWith(wordGroupAllRepository
                                                                .findByIdGroup(wordGroupAll.getIdGroup()))))
                                .assertNext(tuple -> {
                                        Handler handlerBeforeAs = tuple.getT1().getT1();
                                        Handler handlerAfterAs = tuple.getT1().getT2();
                                        assertNotEquals(tuple.getT2().getHandlersId(), handlerAfter.getId());
                                        assertNotEquals(tuple.getT2().getTotalSendRequest(), dto.getTotalSendRequest());
                                        assertNotEquals(tuple.getT2().getTotalDetectPrivate(),
                                                        dto.getTotalDetectPrivate());
                                        assertEquals(handlerAfterAs.getCountGroup(), handlerAfter.getCountGroup());
                                        assertEquals(handlerBeforeAs.getCountGroup(), handlerBefore.getCountGroup());
                                }).verifyComplete();
        }

        @Test
        void updateLastHandleShouldFail() {

                StepVerifier.create(wordGroupAllService.updateWGALastHandle(1L, null))
                                .expectErrorMatches(t -> t instanceof IllegalArgumentException && t.getMessage()
                                                .equals("LastHandle of type Instant not must be is null!"))
                                .verify();
        }

        @Test
        void updateWGALastHandleTest() {
                WordGroupAll wordGroupAll = new WordGroupAll(null, 9991123L, "info2", "title2", "username2",
                                null, 0L, 1, null,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                Instant newLastHandle = Instant.parse("2025-12-11T06:00:00Z");

                StepVerifier.create(wordGroupAllRepository.save(wordGroupAll)
                                .flatMap(saved -> wordGroupAllService.updateWGALastHandle(saved.getId(), newLastHandle)
                                                .flatMap(res -> wordGroupAllRepository.findById(saved.getId()))))
                                .assertNext(wg -> {
                                        assertEquals(wg.getLastHandle(), newLastHandle);
                                }).verifyComplete();
        }

        @Test
        void deleteByIdTest() {
                WordGroupAll wordGroupAll = new WordGroupAll(null, 99911234L, "info2", "title2", "username2",
                                null, 0L, 1, null,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                StepVerifier
                                .create(wordGroupAllRepository.save(wordGroupAll)
                                                .flatMap(saved -> wordGroupAllService.deleteById(saved.getId())
                                                                .flatMap(res -> wordGroupAllRepository
                                                                                .findById(saved.getId()))))
                                .expectNextCount(0).verifyComplete();
        }
}
