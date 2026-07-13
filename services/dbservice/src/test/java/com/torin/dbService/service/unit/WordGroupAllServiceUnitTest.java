package com.torin.dbService.service.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.torin.dbService.dto.WordGroupAllDto;
import com.torin.dbService.r2dbc.entity.WordGroupAll;
import com.torin.dbService.r2dbc.port.WordGroupAllPort;
import com.torin.dbService.r2dbc.service.HandlerService;
import com.torin.dbService.r2dbc.service.WordGroupAllService;
import com.torin.dbService.service.MapperService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class WordGroupAllServiceUnitTest {
        @InjectMocks
        private WordGroupAllService wordGroupAllService;

        @Mock
        private WordGroupAllPort wordGroupAllPort;

        @Mock
        private HandlerService handlerService;

        @Mock
        private MapperService mapperService;

        @Test
        void addWordGroupAllTest() {
                WordGroupAllDto dtoOld = new WordGroupAllDto(
                                null, 123L, "info", "title", "username",
                                null, 0L, 1, 1L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);

                WordGroupAll entityNew = new WordGroupAll(
                                1L, 123L, "info", "title", "username",
                                null, 0L, 1, 1L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);

                WordGroupAllDto dtoNew = new WordGroupAllDto(
                                1L, 123L, "info", "title", "username",
                                null, 0L, 1, 1L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);

                when(handlerService.updateCountGroupByIdIncrement(1L))
                                .thenReturn(Mono.just(1));

                when(wordGroupAllPort.save(any()))
                                .thenReturn(Mono.just(entityNew));

                when(mapperService.toEntity(any(WordGroupAllDto.class)))
                                .thenReturn(entityNew);

                when(mapperService.toDto(any(WordGroupAll.class)))
                                .thenReturn(dtoNew);

                StepVerifier.create(wordGroupAllService.addWordGroupAll(dtoOld))
                                .assertNext(dto -> assertEquals(1L, dto.getId()))
                                .verifyComplete();
        }

        @Test
        void addWordGroupAllNewTest() {
                WordGroupAllDto dtoOld = new WordGroupAllDto(
                                null, null, null, null, "username",
                                null, 68268711L, null, null,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                WordGroupAll entityOld = new WordGroupAll(
                                null, null, null, null, "username",
                                null, 68268711L, null, null,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                WordGroupAll entityNew = new WordGroupAll(
                                1L, null, null, null, "username",
                                null, 68268711L, null, 1L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);
                WordGroupAllDto dtoNew = new WordGroupAllDto(
                                1L, null, null, null, "username",
                                null, 68268711L, null, 1L,
                                Instant.now(), Instant.now(), 0, 0,
                                0L, 123L, Instant.now(), null, null);

                when(handlerService.findIdByMinCountGroupParseGroup()).thenReturn(Mono.just(1L));
                when(mapperService.toEntity(any(WordGroupAllDto.class))).thenReturn(entityOld);
                when(wordGroupAllPort.save(any(WordGroupAll.class))).thenReturn(Mono.just(entityNew));
                when(handlerService.updateCountGroupByIdIncrement(anyLong())).thenReturn(Mono.just(1));
                when(mapperService.toDto(any(WordGroupAll.class))).thenReturn(dtoNew);

                StepVerifier.create(wordGroupAllService.addWordGroupAllNew(dtoOld)).assertNext(wg -> {
                        assertEquals(wg.getHandlersId(), dtoNew.getHandlersId());
                        assertEquals(wg.getId(), dtoNew.getId());
                }).verifyComplete();
        }

}
