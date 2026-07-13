package com.torin.dbService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.torin.dbService.dto.PatchLastHandleRequest;
import com.torin.dbService.dto.WordGroupAllDto;
import com.torin.dbService.r2dbc.service.WordGroupAllService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/word-group-all")
public class WordGroupAllController {
    @Autowired
    private WordGroupAllService wordGroupAllService;

    @GetMapping("/by-id-group/{idGroup}")
    public Mono<ResponseEntity<WordGroupAllDto>> getWordGroupAllByIdGroup(
            @PathVariable Long idGroup) {
        return wordGroupAllService.findByIdGroup(idGroup).map(x -> ResponseEntity.ok(x))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/batch")
    public Flux<WordGroupAllDto> getBatch(
            @RequestParam Long handlersId,
            @RequestParam Long offsetId,
            @RequestParam Integer limit) {
        return wordGroupAllService.findBatchByHandlerId(handlersId, offsetId, limit);
    }

    @GetMapping("/by-id-group/{idGroup}/by-handlers-id/{handlersId}")
    public Mono<ResponseEntity<WordGroupAllDto>> getWordGroupAllByIdGroupAndHandlersId(
            @PathVariable Long idGroup,
            @PathVariable Long handlersId) {
        return wordGroupAllService.findByIdGroupAndHandlersId(idGroup, handlersId).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-hash/{hashGroup}/by-handlers-id/{handlersId}")
    public Mono<ResponseEntity<WordGroupAllDto>> getWordGroupAllByHashGroupAndHandlersId(
            @PathVariable String hashGroup,
            @PathVariable Long handlersId) {
        return wordGroupAllService.findByHashGroupAndHandlersId(hashGroup, handlersId).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> wordGroupAllDeleteById(@PathVariable Long id) {
        return wordGroupAllService.deleteById(id).map(x -> ResponseEntity.noContent().build());
    }

    @PostMapping
    public Mono<ResponseEntity<WordGroupAllDto>> postWordGroupAll(@RequestBody WordGroupAllDto wordGroupAllDto) {
        return wordGroupAllService.addWordGroupAll(wordGroupAllDto).map(ResponseEntity::ok);
    }

    @PostMapping("/prehandle-new-chat")
    public Mono<ResponseEntity<WordGroupAllDto>> postWordGroupAllPrehandleNewChat(
            @RequestBody WordGroupAllDto wordGroupAllDto) {
        return wordGroupAllService.addWordGroupAllNew(wordGroupAllDto).map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<WordGroupAllDto>> putWordGroupAll(@PathVariable Long id,
            @RequestBody WordGroupAllDto wordGroupAllDto) {
        if (!wordGroupAllDto.getId().equals(id)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return wordGroupAllService.updateWordGroupAll(id, wordGroupAllDto).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/last-handle")
    public Mono<ResponseEntity<Void>> patchLastHandle(
            @PathVariable Long id,
            @RequestBody PatchLastHandleRequest lastHandleRequest) {
        return wordGroupAllService.updateWGALastHandle(id, lastHandleRequest.lastHandle())
                .map(updated -> updated ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/recalculate-hand-sr-dp")
    public Mono<ResponseEntity<Void>> updateHandlersIdAndTSRAndTDP(@PathVariable Long id,
            @RequestBody WordGroupAllDto wordGroupAllDto) {

        return wordGroupAllService
                .updateWGAHandlersIdAndTotalSRAndTotalDP(id, wordGroupAllDto)
                .map(updated -> updated ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build());
    }
}
