package com.torin.dbService.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.torin.dbService.dto.HandlerDto;
import com.torin.dbService.r2dbc.service.HandlerService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/handlers")
public class HandlersController {
    @Autowired
    private HandlerService handlerService;

    @GetMapping
    public Flux<HandlerDto> getAll() {
        return handlerService.findAll();
    }

    @GetMapping("/count-group/min")
    public Mono<Long> getIdHandlerByMinCountGroup() {
        return handlerService.findIdByMinCountGroupParseGroup();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<HandlerDto>> getHandlersById(@PathVariable Long id) {
        return handlerService.findById(id).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<HandlerDto>> addHandler(@RequestBody HandlerDto handlerDto) {
        return handlerService.addHandler(handlerDto).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> handlersDeleteById(@PathVariable Long id) {
        return handlerService.deleteById(id).map(x -> ResponseEntity.noContent().build());
    }

    @GetMapping("/by-category")
    public Mono<List<Long>> getAllHandlersId(
            @RequestParam String category) {
        return handlerService.getAllIdByCategory(category)
                .collectList();
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<HandlerDto>> putHandlers(@PathVariable Long id,
            @RequestBody HandlerDto handlerDto) {
        if (!handlerDto.getId().equals(id)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return handlerService.updateHandler(id, handlerDto).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
