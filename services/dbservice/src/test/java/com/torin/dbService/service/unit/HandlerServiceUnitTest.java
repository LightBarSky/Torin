package com.torin.dbService.service.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.torin.dbService.dto.HandlerDto;
import com.torin.dbService.r2dbc.entity.Handler;
import com.torin.dbService.r2dbc.port.HandlerPort;
import com.torin.dbService.r2dbc.service.HandlerService;
import com.torin.dbService.service.MapperService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class HandlerServiceUnitTest {
    @Mock
    private HandlerPort handlerPort;

    @Mock
    private MapperService mapperService;

    @InjectMocks
    private HandlerService handlerService;

    private List<Handler> handlersEntity = List.of(
            new Handler(1L, 52L, "hash", "79991", "", "", "ParseGroup", 0, "OneHandler"),
            new Handler(2L, 52L, "hash", "79992", "", "", "ParseGroup", 5, "TwoHandler"),
            new Handler(3L, 52L, "hash", "79993", "", "", "ParseGroup", 8, "ThreeHandler"));

    private List<HandlerDto> handlersDto = List.of(
            new HandlerDto(1L, 52L, "hash", "79991", "", "", "ParseGroup", 0, "OneHandler"),
            new HandlerDto(2L, 52L, "hash", "79992", "", "", "ParseGroup", 5, "TwoHandler"),
            new HandlerDto(3L, 52L, "hash", "79993", "", "", "ParseGroup", 8, "ThreeHandler"));

    @Test
    void findAllTest() {

        when(handlerPort.findAllByOrderByIdAsc()).thenReturn(Flux.fromIterable(handlersEntity));
        when(mapperService.toDto(handlersEntity.get(0))).thenReturn(handlersDto.get(0));
        when(mapperService.toDto(handlersEntity.get(1))).thenReturn(handlersDto.get(1));
        when(mapperService.toDto(handlersEntity.get(2))).thenReturn(handlersDto.get(2));

        StepVerifier.create(handlerService.findAll()).expectNextCount(3).verifyComplete();
    }

    @Test
    void findByIdTest() {

        when(handlerPort.findById(2L)).thenReturn(Mono.just(handlersEntity.get(1)));
        when(mapperService.toDto(handlersEntity.get(1))).thenReturn(handlersDto.get(1));

        StepVerifier.create(handlerService.findById(2L)).assertNext(handler -> {
            assertEquals(handler.getId(), 2L);
        }).verifyComplete();
    }

    @Test
    void addHandlersWithIdShouldFail() {

        StepVerifier.create(handlerService.addHandler(handlersDto.get(0)))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("ID must be null for new entity"))
                .verify();
    }

    @Test
    void getAllIdByCategoryTest() {

        when(handlerPort.findAllByCategory("ParseGroup")).thenReturn(Flux.fromIterable(handlersEntity));

        StepVerifier.create(handlerService.getAllIdByCategory("ParseGroup"))
                .expectNext(1L, 2L, 3L).verifyComplete();
    }
}
