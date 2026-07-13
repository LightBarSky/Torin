package com.torin.analytic.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.torin.analytic.infrastructure.ActivityEventBaseUser;
import com.torin.analytic.infrastructure.ActivityLeadersAccumulator;
import com.torin.analytic.infrastructure.AudienceQualityAccumulator;
import com.torin.analytic.infrastructure.BaseMetricsAccumulator;
import com.torin.analytic.infrastructure.ActivityBaseUserAccumulator;
import com.torin.analytic.infrastructure.ActivityEventAQandAL;
import com.torin.analytic.infrastructure.ActivityEventBaseMetrics;
import com.torin.analytic.infrastructure.EngagementFunnelSub;
import com.torin.analytic.infrastructure.TypeEvent;
import com.torin.core.dto.*;
import com.torin.es.service.ElasticsearchService;
import com.torin.postgres.service.WordGroupAllService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

public class AnalyticService {
        
        private final ElasticsearchService esService;
        
        private final IntermediateComputingService intermediateComputingService;
        
        private final WordGroupAllService wordGroupAllService;

        public AnalyticService(ElasticsearchService esService, IntermediateComputingService intermediateComputingService,
                        WordGroupAllService wordGroupAllService) {
                this.esService = esService;
                this.intermediateComputingService = intermediateComputingService;
                this.wordGroupAllService = wordGroupAllService;
        }

        public Mono<BaseMetricsDto> getBaseMetricsAnalytic(Long idGroup, Instant from, Instant to) {

                Flux<ActivityEventBaseMetrics> events = Flux.merge(

                                esService.searchMessagesPropertiesStream(idGroup, null, from, to)
                                                .map(msg -> {
                                                        Instant inst = msg.date();

                                                        int admin = 0;
                                                        int user = 0;

                                                        if ("0".equals(msg.isComments()))
                                                                admin = 1;
                                                        else if ("1".equals(msg.isComments()))
                                                                user = 1;
                                                        else if ("2".equals(msg.isComments())) {
                                                                if (msg.idFrom() == null)
                                                                        admin = 1;
                                                                else
                                                                        user = 1;
                                                        }

                                                        return new ActivityEventBaseMetrics(
                                                                        TypeEvent.MESSAGE,
                                                                        inst,
                                                                        msg.idFrom(),
                                                                        1, 0L, 0,
                                                                        msg.views() == null ? 0L : msg.views(),
                                                                        admin,
                                                                        user);
                                                }),

                                // реакции
                                esService.searchReactionsStream(idGroup, null, from, to)
                                                .map(r -> new ActivityEventBaseMetrics(
                                                                TypeEvent.REACTIONS,
                                                                r.date(),
                                                                r.idUser(),
                                                                0, 1L, 0,
                                                                0L,
                                                                0, 0)),

                                // подарки
                                esService.searchGiftsStream(null, idGroup, from, to)
                                                .map(g -> new ActivityEventBaseMetrics(
                                                                TypeEvent.GIFTS,
                                                                g.date(),
                                                                g.idFrom(),
                                                                0, 0L, 1,
                                                                0L,
                                                                0, 0)),

                                // реакции агрегированные
                                esService.searchReactionsGeneralStream(idGroup, from, to)
                                                .map(r -> new ActivityEventBaseMetrics(
                                                                TypeEvent.REACTIONS_GENERAL,
                                                                r.date(),
                                                                null,
                                                                0, r.count(), 0,
                                                                0L,
                                                                0, 0)));
                Mono<Map<Instant, Long>> partChangedMono = esService.searchParticipantChangedStream(idGroup, from, to)
                                .collect(
                                                () -> new TreeMap<Instant, Long>(),
                                                (map, m) -> map.merge(
                                                                m.date(),
                                                                m.participantsCount(),
                                                                (oldVal, newVal) -> newVal));
                Mono<BaseMetricsAccumulator> accMono = events
                                .collect(
                                                BaseMetricsAccumulator::new,
                                                (acc, event) -> acc.add(event));
                return Mono.zip(partChangedMono, accMono).map(tuple -> {
                        Map<Instant, Long> participantChanged = tuple.getT1();
                        BaseMetricsAccumulator acc = tuple.getT2();
                        return intermediateComputingService.getBaseMetricsDto(acc, participantChanged, from, to);
                });
        }

        public Mono<EngagementFunnelDto> getEngagementFunnelAnalytic(
                        Long idGroup,
                        Instant from,
                        Instant to) {

                Mono<Map<String, EngagementFunnelSub>> messagesMono = esService
                                .searchMessagesPropertiesStream(idGroup, null, from, to)
                                .filter(message -> "0".equals(message.isComments()) ||
                                                ("2".equals(message.isComments()) && message.idFrom() == null))
                                .collect(
                                                HashMap::new,
                                                (map, message) -> {

                                                        String key = message.identityId();

                                                        EngagementFunnelSub sub = new EngagementFunnelSub(
                                                                        message.idMessage(),
                                                                        message.replies() == null ? 0L
                                                                                        : message.replies(),
                                                                        message.views() == null ? 0L
                                                                                        : message.views(),
                                                                        message.date().atOffset(ZoneOffset.UTC)
                                                                                        .toLocalDate());

                                                        map.merge(key, sub, (oldVal, newVal) -> {
                                                                oldVal.setIdMessage(newVal.getIdMessage());
                                                                oldVal.setReplies(newVal.getReplies());
                                                                oldVal.setViews(newVal.getViews());
                                                                oldVal.setDate(newVal.getDate());
                                                                return oldVal;
                                                        });
                                                });

                Mono<Map<Long, Long>> reactionsMono = esService.searchReactionsGeneralStream(idGroup, from, to)
                                .filter(r -> "0".equals(r.isComments()) ||
                                                "2".equals(r.isComments()))
                                .collect(HashMap::new, (map, reactionsGen) -> {
                                        map.merge(reactionsGen.idMessage(), reactionsGen.count(),
                                                        (oldVal, newVal) -> oldVal + newVal);
                                });

                Mono<WordGroupAllDto> groupMono = wordGroupAllService.findByIdGroup(idGroup);

                return Mono.zip(messagesMono, reactionsMono, groupMono)
                                .map(tuple -> {
                                        Map<String, EngagementFunnelSub> messages = tuple.getT1();
                                        Map<Long, Long> reactions = tuple.getT2();
                                        WordGroupAllDto group = tuple.getT3();

                                        return intermediateComputingService.getEngagementFunnelDto(messages, reactions,
                                                        group, from, to);
                                });
        }

        public Mono<AudienceQualityDto> getAudienceQuality(Long idGroup, Instant from, Instant to) {

                Mono<WordGroupAllDto> groupMono = wordGroupAllService.findByIdGroup(idGroup);

                Flux<ActivityEventAQandAL> eventFlux = Flux.merge(
                                esService.searchMessagesPropertiesStream(idGroup, null, from, to)
                                                .filter(x -> x.idFrom() != null)
                                                .map(m -> new ActivityEventAQandAL(TypeEvent.MESSAGE,
                                                                m.idFrom(), m.date())),
                                esService.searchReactionsStream(idGroup, null, from, to)
                                                .filter(x -> x.idUser() != null)
                                                .map(r -> new ActivityEventAQandAL(TypeEvent.REACTIONS,
                                                                r.idUser(), r.date())),
                                esService.searchGiftsStream(null, idGroup, from, to).filter(x -> x.idFrom() != null)
                                                .map(g -> new ActivityEventAQandAL(TypeEvent.GIFTS,
                                                                g.idFrom(), g.date())));
                Mono<AudienceQualityAccumulator> accMono = eventFlux.collect(AudienceQualityAccumulator::new,
                                (a, e) -> a.add(e));
                Mono<Map<LocalDate, Long>> partChangedMono = esService.searchParticipantChangedStream(idGroup, from, to)
                                .collect(
                                                HashMap::new,
                                                (map, x) -> {
                                                        LocalDate key = x.date()
                                                                        .atOffset(ZoneOffset.UTC)
                                                                        .toLocalDate();

                                                        long value = x.participantsCount() == null ? 0L
                                                                        : x.participantsCount();

                                                        map.merge(key, value,
                                                                        (oldVal, newVal) -> Math.max(oldVal, newVal));
                                                });

                return Mono.zip(groupMono, partChangedMono, accMono)
                                .map(tuple -> {
                                        WordGroupAllDto wordGroupAllDto = tuple.getT1();
                                        AudienceQualityAccumulator acc = tuple.getT3();
                                        Map<LocalDate, Long> membersToPeriod = tuple.getT2();

                                        return intermediateComputingService.getAudienceQualityDto(acc, wordGroupAllDto,
                                                        membersToPeriod, from, to);
                                });

        }

        public Mono<ActivityLeadersDto> getActivityLeaders(Long idGroup, Instant from, Instant to) {

                Flux<ActivityEventAQandAL> actEventFlux = Flux.merge(
                                esService.searchMessagesPropertiesStream(idGroup, null, from, to)
                                                .filter(m -> m.idFrom() != null)
                                                .map(m -> new ActivityEventAQandAL(TypeEvent.MESSAGE, m.idFrom(),
                                                                m.date())),
                                esService.searchReactionsStream(idGroup, null, from, to)
                                                .filter(r -> r.idUser() != null)
                                                .map(r -> new ActivityEventAQandAL(TypeEvent.REACTIONS, r.idUser(),
                                                                r.date())));
                Mono<ActivityLeadersAccumulator> accMono = actEventFlux.collect(ActivityLeadersAccumulator::new,
                                (acc, e) -> acc.add(e));

                return accMono.map(acc -> {
                        return intermediateComputingService.getActivityLeadersDto(acc, 10);
                });
        }

        public Flux<WordGroupAllDto> getGroupsOfUser(Long idUser, String username) {
                return esService.searchUser(idUser, username)
                                .flatMapMany(us -> esService.searchChatStreamUnique(us.idUser(), null))
                                .flatMap(ch -> wordGroupAllService.findByIdGroup(ch.idGroup())
                                                .map(group -> Tuples.of(ch, group)))
                                .map(tuple -> {
                                        var chat = tuple.getT1();
                                        var group = tuple.getT2();

                                        group.setJoinedDate(chat.dateJoined());
                                        return group;
                                });
        }

        public Mono<BaseAnanlyticUserDto> getBaseAnalyticUser(Long idUser, Instant from, Instant to) {

                Mono<List<WordGroupAllDto>> wgAccMono = this.getGroupsOfUser(idUser, null).collectList();

                Flux<ActivityEventBaseUser> fluxActivity = esService.searchChatStreamUnique(idUser, null)
                                .flatMap(chat -> {
                                        Long groupId = chat.idGroup();

                                        Flux<ActivityEventBaseUser> messages = esService
                                                        .searchMessagesPropertiesStream(groupId, idUser,
                                                                        from, to)
                                                        .map(msg -> new ActivityEventBaseUser(
                                                                        groupId,
                                                                        msg.date().atOffset(ZoneOffset.UTC)
                                                                                        .toLocalDate(),
                                                                        1, 0, 0));

                                        Flux<ActivityEventBaseUser> reactions = esService
                                                        .searchReactionsStream(groupId, idUser, from, to)
                                                        .map(r -> new ActivityEventBaseUser(
                                                                        groupId,
                                                                        r.date().atOffset(ZoneOffset.UTC)
                                                                                        .toLocalDate(),
                                                                        0, 1, 0));

                                        Flux<ActivityEventBaseUser> gifts = esService
                                                        .searchGiftsStream(idUser, groupId, from, to)
                                                        .map(g -> new ActivityEventBaseUser(
                                                                        groupId,
                                                                        g.date().atOffset(ZoneOffset.UTC)
                                                                                        .toLocalDate(),
                                                                        0, 0, 1));

                                        return Flux.merge(messages, reactions, gifts)
                                                        .switchIfEmpty(Flux.just(new ActivityEventBaseUser(
                                                                        groupId, null, 0, 0, 0)));
                                }, 4);

                Mono<ActivityBaseUserAccumulator> accMono = fluxActivity.collect(ActivityBaseUserAccumulator::new,
                                (acc, e) -> acc.add(e));

                return Mono.zip(accMono, wgAccMono).map(tuple -> {
                        List<WordGroupAllDto> wgs = tuple.getT2();
                        ActivityBaseUserAccumulator acc = tuple.getT1();

                        return intermediateComputingService.getBaseAnanlyticUserDto(acc, wgs, from, to);
                });
        }
}
