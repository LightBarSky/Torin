package com.torin.app.controller;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.torin.core.dto.ChatDto;
import com.torin.core.dto.ParticipantChangedDto;
import com.torin.core.dto.WordGroupAllDto;
import com.torin.es.service.ElasticsearchService;
import com.torin.postgres.service.WordGroupAllService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/group")
public class GroupController {
    @Autowired
    private WordGroupAllService wordGroupAllService;
    @Autowired
    @Qualifier("dbWebClient")
    private WebClient webClient;
    @Autowired
    private ElasticsearchService esService;

    @GetMapping
    public Flux<WordGroupAllDto> getGroupByIdGroupOrUsername(
            @RequestParam(name = "id_group", required = false) Long idGroup,
            @RequestParam(name = "username", required = false) String username) {

        boolean hasId = idGroup != null;
        boolean hasUsername = username != null && !username.isBlank();

        if (!hasId && !hasUsername) {
            return Flux.error(new IllegalArgumentException(
                    "Either id_group or username must be provided (but not both)"));
        }

        if (hasId) {
            return wordGroupAllService.findByIdGroup(idGroup).flux();
        }

        return wordGroupAllService.findByFindGroup(username);
    }

    @GetMapping("get-chats")
    public Flux<ChatDto> getChats(@RequestParam(name = "id_group", required = false) Long idGroup,
            @RequestParam(name = "id_user", required = false) Long idUser) {
        boolean hasIdGroup = idGroup != null;
        boolean hasIdUser = idUser != null;

        if (!hasIdGroup && !hasIdUser) {
            return Flux.error(new IllegalArgumentException(
                    "Either id_group or username must be provided (but not both)"));
        }
        return esService.searchChatStreamUnique(idUser, idGroup);
    }

    @GetMapping("get-participant-changed")
    public Flux<ParticipantChangedDto> getParticipantChanged(@RequestParam("from") Instant from,
            @RequestParam("to") Instant to,
            @RequestParam("id_group") Long idGroup) {
        return esService.searchParticipantChangedStream(idGroup, from, to);
    }

    @PostMapping("add")
    public Mono<WordGroupAllDto> addNewGroup(@RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "hash", required = false) String hash) {
        boolean hasUsername = username != null && !username.isBlank();
        boolean hasHash = hash != null && !hash.isBlank();

        if (hasUsername == hasHash) {
            return Mono.error(new IllegalArgumentException(
                    "Either username or hash must be provided (but not both)"));
        }
        return webClient.post()
        .uri("api/v1/word-group-all/prehandle-new-chat")
        .bodyValue(new WordGroupAllDto(username, hash))
                .retrieve()
                .bodyToMono(WordGroupAllDto.class);
    }
}
