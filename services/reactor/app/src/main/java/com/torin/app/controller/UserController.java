package com.torin.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.torin.analytic.service.AnalyticService;
import com.torin.core.dto.UserDto;
import com.torin.core.dto.WordGroupAllDto;
import com.torin.es.service.ElasticsearchService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    private AnalyticService analyticService;
    @Autowired
    private ElasticsearchService esService;

    @GetMapping("/groups")
    public Flux<WordGroupAllDto> getGroupsOfUserByIdGroupOrUsername(
            @RequestParam(name = "id_user", required = false) Long idUser,
            @RequestParam(name = "username", required = false) String username) {
        boolean hasId = idUser != null;
        boolean hasUsername = username != null && !username.isBlank();
        if (!hasId && !hasUsername) {
            return Flux.error(new IllegalArgumentException("Either id_user or username must be provided"));
        }

        return analyticService.getGroupsOfUser(idUser, username);
    }

    @GetMapping
    public Mono<UserDto> getUserByIdUserOrUsername(@RequestParam(name = "id_user", required = false) Long idUser,
            @RequestParam(name = "username", required = false) String username) {
        boolean hasId = idUser != null;
        boolean hasUsername = username != null && !username.isBlank();
        if (!hasId && !hasUsername) {
            return Mono.error(new IllegalArgumentException("Either id_user or username must be provided"));
        }
        return esService.searchUser(idUser, username);
    }
}
