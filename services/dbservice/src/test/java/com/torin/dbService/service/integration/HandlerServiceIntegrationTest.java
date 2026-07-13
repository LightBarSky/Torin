package com.torin.dbService.service.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.DynamicPropertySource;

import com.torin.dbService.dto.HandlerDto;
import com.torin.dbService.r2dbc.entity.Handler;
import com.torin.dbService.r2dbc.repository.HandlerRepository;
import com.torin.dbService.r2dbc.service.HandlerService;

import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class HandlerServiceIntegrationTest extends AbstractIntegrationDBTest {

        @Autowired
        private HandlerService handlerService;

        @Autowired
        private HandlerRepository handlerRepository;

        @DynamicPropertySource
        static void configure(DynamicPropertyRegistry registry) {
                registry.add("spring.flyway.locations", () -> "classpath:db/test-migration/handlers");
        }

        @BeforeEach
        void setUp() throws SQLException {
                handlerRepository.deleteAll().block();

                handlerRepository.saveAll(List.of(
                                new Handler(null, 52L, "hash", "79991", "", "", "ParseGroup", 0, "OneHandler"),
                                new Handler(null, 52L, "hash", "79992", "", "", "ParseGroup", 5, "TwoHandler"),
                                new Handler(null, 52L, "hash", "79993", "", "", "ParseGroup", 8, "ThreeHandler")))
                                .blockLast();
        }

        @Test
        void findAllTest() {
                StepVerifier.create(handlerService.findAll()).expectNextCount(3).verifyComplete();
        }

        @Test
        void findByIdTest() {
                Long id = handlerRepository.findAll()
                                .map(Handler::getId)
                                .blockFirst();

                StepVerifier.create(handlerService.findById(id))
                                .assertNext(handler -> {
                                        assertNotNull(handler);
                                        assertEquals(id, handler.getId());
                                        assertEquals("OneHandler", handler.getNameHandler());
                                })
                                .verifyComplete();
        }

        @Test
        void addHandlerWithIdShouldFail() {
                HandlerDto dtoWithId = new HandlerDto(1L, 52L, "hash", "79991", "", "", "ParseGroup", 2, "OneHandler");

                StepVerifier.create(handlerService.addHandler(dtoWithId))
                                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                                                throwable.getMessage().equals("ID must be null for new entity"))
                                .verify();
        }

        @Test
        void deleteByIdTest() {
                Long id = handlerRepository.findAll()
                                .map(Handler::getId)
                                .blockFirst();

                StepVerifier.create(handlerService.deleteById(id).map(x -> handlerService.findById(id)))
                                .expectNextCount(0)
                                .verifyComplete();
        }

        @Test
        void getAllByCategoryTest() {
                Map<String, Long> categoris = handlerRepository.findAll()
                                .map(Handler::getCategory)
                                .collectList()
                                .map(list -> list.stream()
                                                .collect(Collectors.groupingBy(c -> c, Collectors.counting())))
                                .block();

                String category = categoris.keySet().stream().findFirst().get();
                StepVerifier.create(handlerService.getAllIdByCategory(category))
                                .expectNextCount(categoris.get(category))
                                .verifyComplete();
        }

        @Test
        void updateHandlerTest() {
                Long id = handlerRepository.findAll()
                                .map(Handler::getId)
                                .blockFirst();

                HandlerDto dtoWithId = new HandlerDto(id, 52L, "hash", "79991", "", "", "ParseGroup", 2,
                                "OneOneHandler");

                StepVerifier.create(handlerService.updateHandler(dtoWithId.getId(), dtoWithId))
                                .assertNext(hand -> {
                                        assertNotNull(hand);
                                        assertEquals(hand.getNameHandler(), dtoWithId.getNameHandler());
                                })
                                .verifyComplete();
        }

        @Test
        void findIdByMinCountGroupParseGroupTest() {

                Long id = handlerRepository.findAll()
                                .map(Handler::getId)
                                .blockFirst();

                StepVerifier.create(handlerService.findIdByMinCountGroupParseGroup())
                                .assertNext(idF -> {
                                        assertNotNull(idF);
                                        assertEquals(idF, id);
                                })
                                .verifyComplete();
        }

        @Test
        void updateCountGroupByIdIncrementTest() {
                Handler hand = handlerRepository.findAll().blockFirst();
                int oldCount = hand.getCountGroup();

                StepVerifier.create(
                                handlerService.updateCountGroupByIdIncrement(hand.getId())
                                                .flatMap(updatedRows -> handlerRepository.findById(hand.getId())))
                                .assertNext(updatedHand -> {
                                        assertNotNull(updatedHand);
                                        assertEquals(oldCount + 1, updatedHand.getCountGroup());
                                })
                                .verifyComplete();
        }

        @Test
        void updateCountGroupByIdDecrementTest() {
                Handler hand = handlerRepository.findAll().blockFirst();
                int oldCount = hand.getCountGroup();

                StepVerifier.create(
                                handlerService.updateCountGroupByIdDecrement(hand.getId())
                                                .flatMap(updatedRows -> handlerRepository.findById(hand.getId())))
                                .assertNext(updatedHand -> {
                                        assertNotNull(updatedHand);
                                        assertEquals(Math.max(oldCount - 1, 0), updatedHand.getCountGroup());
                                })
                                .verifyComplete();
        }
}
