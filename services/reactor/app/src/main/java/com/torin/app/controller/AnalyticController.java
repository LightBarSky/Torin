package com.torin.app.controller;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.torin.analytic.service.AnalyticService;
import com.torin.core.dto.ActivityLeadersDto;
import com.torin.core.dto.AudienceQualityDto;
import com.torin.core.dto.BaseAnanlyticUserDto;
import com.torin.core.dto.BaseMetricsDto;
import com.torin.core.dto.EngagementFunnelDto;
import com.torin.core.dto.UserChangedDto;
import com.torin.core.dto.WordGroupAllChangedDto;
import com.torin.postgres.service.UserChangedService;
import com.torin.postgres.service.WordGroupAllChangedService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticController {
    @Autowired
    private WordGroupAllChangedService wordGroupAllChangedService;
    @Autowired
    private UserChangedService userChangedService;
    @Autowired
    private AnalyticService analyticService;

    @GetMapping("/base-metrics")
    public Mono<BaseMetricsDto> getBaseMetrics(
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to,
            @RequestParam("id_group") Long idGroup) {
        return analyticService.getBaseMetricsAnalytic(idGroup, from, to);
    }

    @GetMapping("/engagement-funnel")
    public Mono<EngagementFunnelDto> getEngagementFunnel(
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to,
            @RequestParam("id_group") Long idGroup) {
        return analyticService.getEngagementFunnelAnalytic(idGroup, from, to);
    }

    @GetMapping("/audience-quality")
    public Mono<AudienceQualityDto> getAudienceQuality(
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to,
            @RequestParam("id_group") Long idGroup) {
        return analyticService.getAudienceQuality(idGroup, from, to);
    }

    @GetMapping("/activity-leaders")
    public Mono<ActivityLeadersDto> getActivityLeaders(
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to,
            @RequestParam("id_group") Long idGroup) {
        return analyticService.getActivityLeaders(idGroup, from, to);
    }

    @GetMapping("/base-analytic-user")
    public Mono<BaseAnanlyticUserDto> getBaseAnalyticUser(
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to,
            @RequestParam(name = "id_user", required = true) Long idUser) {
        return analyticService.getBaseAnalyticUser(idUser, from, to);
    }

    @GetMapping("/user-changed")
    public Flux<UserChangedDto> getUserChanged(
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to,
            @RequestParam(name = "id_user", required = true) Long idUser) {
        if (from == null && to == null) {
            return userChangedService.findAll(idUser);
        } else {
            return userChangedService.findAllBetweenUpdatedAt(idUser, from, to);
        }
    }

    @GetMapping("/word-group-all-changed")
    public Flux<WordGroupAllChangedDto> getWordGroupAllChanged(
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to,
            @RequestParam(name = "id_group", required = true) Long idGroup) {
        if (from == null && to == null) {
            return wordGroupAllChangedService.findAll(idGroup);
        } else {
            return wordGroupAllChangedService.findAllBetweenDate(idGroup, from, to);
        }
    }
}
