package com.torin.analytic.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.torin.analytic.core.AnalyticCoreService;
import com.torin.analytic.infrastructure.ActivityBaseUserAccumulator;
import com.torin.analytic.infrastructure.ActivityLeadersAccumulator;
import com.torin.analytic.infrastructure.AudienceQualityAccumulator;
import com.torin.analytic.infrastructure.BaseMetricsAccumulator;
import com.torin.analytic.infrastructure.EngagementFunnelSub;
import com.torin.analytic.infrastructure.MetricsForEngagement;
import com.torin.analytic.infrastructure.PeriodType;
import com.torin.core.dto.ActivityEntityDto;
import com.torin.core.dto.ActivityLeadersDto;
import com.torin.core.dto.AudienceQualityDto;
import com.torin.core.dto.BaseAnanlyticUserDto;
import com.torin.core.dto.BaseMetricsDto;
import com.torin.core.dto.EngagementFunnelDto;
import com.torin.core.dto.EngagementFunnelPerDayDto;
import com.torin.core.dto.WordGroupAllDto;


public class IntermediateComputingService {
    
    private final AnalyticCoreService analyticCoreService;

    public IntermediateComputingService(AnalyticCoreService analyticCoreService) {
        this.analyticCoreService = analyticCoreService;
    }

    public BaseAnanlyticUserDto getBaseAnanlyticUserDto(ActivityBaseUserAccumulator acc, List<WordGroupAllDto> wgs,
            Instant from, Instant to) {
        List<Map.Entry<LocalDate, ActivityEntityDto>> innerActivityPeriod = new ArrayList<>();

        LocalDate fromLD = from.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate toLD = to.atZone(ZoneOffset.UTC).toLocalDate();

        for (LocalDate d = fromLD; !d.isAfter(toLD); d = d.plusDays(1)) {
            innerActivityPeriod.add(new AbstractMap.SimpleEntry<>(d,
                    acc.activityPeriod.get(d)));
        }
        return new BaseAnanlyticUserDto(acc.total, wgs, innerActivityPeriod,
                new ArrayList<>(acc.activityByGroup.entrySet()));
    }

    public ActivityLeadersDto getActivityLeadersDto(ActivityLeadersAccumulator acc, int depth) {
        List<Map.Entry<String, Long>> top10ByMessage = acc.activityMessages.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(depth)
                .toList();
        List<Map.Entry<String, Long>> top10ByReaction = acc.activityReactions.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(depth)
                .toList();
        List<Map.Entry<String, Long>> top10ByMessageAndReaction = acc.allActivity.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(depth)
                .toList();
        return new ActivityLeadersDto(top10ByMessage, top10ByReaction, top10ByMessageAndReaction);
    }

    public AudienceQualityDto getAudienceQualityDto(AudienceQualityAccumulator acc, WordGroupAllDto wordGroupAllDto,
            Map<LocalDate, Long> membersToPeriod, Instant from, Instant to) {
        LocalDate fromLD = from.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate toLD = to.atZone(ZoneOffset.UTC).toLocalDate();

        if (wordGroupAllDto == null) {
            return null;
        }
        long members = wordGroupAllDto.getParticipantsCount() == null
                ? 0L
                : wordGroupAllDto.getParticipantsCount();

        long writers = acc.activityMessages.size();
        long allActiveUniq = acc.allActivity.size();

        double writerToMembers = analyticCoreService.writerToMembers(writers, members);
        double writerToShare = analyticCoreService.writerShare(writers, allActiveUniq);

        List<Map.Entry<LocalDate, Double>> innerWriterToMembersPeriod = new ArrayList<>();
        List<Map.Entry<LocalDate, Double>> innerWriterToSharePeriod = new ArrayList<>();
        List<Map.Entry<LocalDate, Double>> innerTimeBurstIndexPeriod = new ArrayList<>();

        for (LocalDate d = fromLD; !d.isAfter(toLD); d = d.plusDays(1)) {
            long allActiveCur = acc.allActivityPeriod
                    .getOrDefault(d, Set.of())
                    .size();

            long writersCur = acc.activityMessagesPeriod
                    .getOrDefault(d, Set.of())
                    .size();

            long membersCur = membersToPeriod.getOrDefault(d, members);

            double writerToMembersCur = analyticCoreService
                    .writerToMembers(writersCur, membersCur);
            double writerToShareCur = analyticCoreService.writerShare(writersCur,
                    allActiveCur);
            innerWriterToMembersPeriod
                    .add(Map.entry(d, writerToMembersCur));
            innerWriterToSharePeriod
                    .add(Map.entry(d, writerToShareCur));

            Map<Instant, Long> timeBurstCur = acc.timeBurstIndexContainer
                    .get(d);
            if (timeBurstCur == null) {
                innerTimeBurstIndexPeriod
                        .add(new AbstractMap.SimpleEntry<>(d, null));
            } else {
                    long allMes = timeBurstCur.values().stream()
                        .mapToLong(Long::longValue).sum();
                long maxMh = timeBurstCur.values().stream()
                        .mapToLong(Long::longValue).max().orElse(0L);
                long avgMh = allMes / 24;
                double timeBurstIndexDay = analyticCoreService
                        .timeBurstIndex(maxMh,
                                avgMh);
                innerTimeBurstIndexPeriod
                        .add(Map.entry(d, timeBurstIndexDay));
            }
        }
        return new AudienceQualityDto(writerToMembers, writerToShare, innerWriterToMembersPeriod,
                innerWriterToSharePeriod, innerTimeBurstIndexPeriod);
    }

    public EngagementFunnelDto getEngagementFunnelDto(Map<String, EngagementFunnelSub> messages,
            Map<Long, Long> reactions,
            WordGroupAllDto group,
            Instant from, Instant to) {
        LocalDate fromLD = from.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate toLD = to.atZone(ZoneOffset.UTC).toLocalDate();

        if (group == null) {
            return null;
        }

        long members = group.getParticipantsCount() == null
                ? 0L
                : group.getParticipantsCount();

        long viewsAll = 0;
        long repliesAll = 0;
        long reactionsAll = 0;

        Map<LocalDate, MetricsForEngagement> mapMetricsEngagement = new HashMap<>();
        List<Map.Entry<Long, Double>> innerViewRatePeriod = new ArrayList<>();
        List<Map.Entry<Long, Double>> innerReactionRatePeriod = new ArrayList<>();
        List<Map.Entry<Long, Double>> innerCommentRatePeriod = new ArrayList<>();
        List<Map.Entry<Long, Double>> innerERviewPeriod = new ArrayList<>();
        List<Map.Entry<LocalDate, EngagementFunnelPerDayDto>> innerERperDay = new ArrayList<>();

        for (EngagementFunnelSub sub : messages.values()) {

            long reactionsCur = reactions.getOrDefault(sub.getIdMessage(), 0L);

            viewsAll += sub.getViews();
            repliesAll += sub.getReplies();
            reactionsAll += reactionsCur;

            double viewRateCur = analyticCoreService.viewRate(sub.getViews(),
                    members, 1L);
            double reactionRateCur = analyticCoreService.reactionRate(reactionsCur,
                    sub.getViews());
            double commentRateCur = analyticCoreService
                    .commentRate(sub.getReplies(), sub.getViews());
            double erViewCur = analyticCoreService.erView(reactionsCur,
                    sub.getReplies(), sub.getViews());

            innerViewRatePeriod.add(Map.entry(sub.getIdMessage(), viewRateCur));
            innerReactionRatePeriod
                    .add(Map.entry(sub.getIdMessage(), reactionRateCur));
            innerCommentRatePeriod
                    .add(Map.entry(sub.getIdMessage(), commentRateCur));
            innerERviewPeriod.add(Map.entry(sub.getIdMessage(), erViewCur));

            mapMetricsEngagement.merge(sub.getDate(),
                    new MetricsForEngagement(sub.getReplies(),
                            sub.getViews(), reactionsCur, 1L),
                    (oldVal, newVal) -> {
                        oldVal.setPosts(oldVal.getPosts()
                                + newVal.getPosts());
                        oldVal.setReactions(oldVal.getReactions()
                                + newVal.getReactions());
                        oldVal.setReplies(oldVal.getReplies()
                                + newVal.getReplies());
                        oldVal.setViews(oldVal.getViews()
                                + newVal.getViews());
                        return oldVal;
                    });
        }

        for (LocalDate d = fromLD; !d.isAfter(toLD); d = d.plusDays(1)) {
            MetricsForEngagement metricsForEngagement = mapMetricsEngagement
                    .getOrDefault(d, null);
            if (metricsForEngagement == null) {
                innerERperDay.add(
                        new AbstractMap.SimpleEntry<>(d, null));
            } else {
                double viewRateCur = analyticCoreService.viewRate(
                        metricsForEngagement.getViews(),
                        members, metricsForEngagement.getPosts());
                double reactionRateCur = analyticCoreService.reactionRate(
                        metricsForEngagement.getReactions(),
                        metricsForEngagement.getViews());
                double commentRateCur = analyticCoreService
                        .commentRate(metricsForEngagement.getReplies(),
                                metricsForEngagement
                                        .getViews());
                double erViewCur = analyticCoreService.erView(
                        metricsForEngagement.getReactions(),
                        metricsForEngagement.getReplies(),
                        metricsForEngagement.getViews());
                innerERperDay.add(
                        Map.entry(d, new EngagementFunnelPerDayDto(
                                viewRateCur, reactionRateCur,
                                commentRateCur, erViewCur)));
            }
        }

        long postsAll = messages.size();

        double allViewRate = analyticCoreService.viewRate(viewsAll, members, postsAll);

        double allReactionRate = analyticCoreService.reactionRate(reactionsAll, viewsAll);

        double allCommentRate = analyticCoreService.commentRate(repliesAll, viewsAll);

        double allERview = analyticCoreService.erView(reactionsAll, repliesAll, viewsAll);

        innerViewRatePeriod.sort(Map.Entry.comparingByKey());
        innerReactionRatePeriod.sort(Map.Entry.comparingByKey());
        innerCommentRatePeriod.sort(Map.Entry.comparingByKey());
        innerERviewPeriod.sort(Map.Entry.comparingByKey());

        return new EngagementFunnelDto(allViewRate, allReactionRate, allCommentRate, allERview, innerViewRatePeriod,
                innerReactionRatePeriod, innerCommentRatePeriod, innerERviewPeriod, innerERperDay);
    }

    public BaseMetricsDto getBaseMetricsDto(BaseMetricsAccumulator acc, Map<Instant, Long> participantChanged,
            Instant from, Instant to) {

        LocalDate fromLD = from.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate toLD = to.atZone(ZoneOffset.UTC).toLocalDate();
        long days = Duration.between(from, to).toDays();
        List<Map.Entry<Instant, Long>> innerParticipantChanged = new ArrayList<>(participantChanged.entrySet());
        int allActiveUsers = acc.allActive.size();
        int activeUsersPerComments = acc.allActiveMessages.size();
        int activeUsersPerReactions = acc.allActiveReactions.size();
        int activeUsersPerGifts = acc.allActiveGifts.size();
        int activeUsersPerCommentsAndReactions = intersection(acc.allActiveMessages, acc.allActiveReactions).size();

        int allPublications = acc.totalPublic;
        int publicationsFromAdmin = acc.totalPublicAdmin;
        int publicationsFromUser = acc.totalPublicUser;

        List<Map.Entry<LocalDate, Integer>> innerAllActiveUsersPeriod = new ArrayList<>();
        List<Map.Entry<LocalDate, Integer>> innerActiveUsersPerCommentsPeriod = new ArrayList<>();
        List<Map.Entry<LocalDate, Integer>> innerActiveUsersPerReactionsPeriod = new ArrayList<>();
        List<Map.Entry<LocalDate, Integer>> innerActiveUsersPerGiftsPeriod = new ArrayList<>();
        List<Map.Entry<LocalDate, Integer>> innerActiveUsersPerCommAndReactPeriod = new ArrayList<>();
        List<Map.Entry<LocalDate, Integer>> innerAllPublicationsPeriod = new ArrayList<>();
        List<Map.Entry<LocalDate, Integer>> innerPublicationsFromAdminPeriod = new ArrayList<>();
        List<Map.Entry<LocalDate, Integer>> innerPublicationsFromUserPeriod = new ArrayList<>();

        for (LocalDate d = fromLD; !d.isAfter(toLD); d = d.plusDays(1)) {

            int allActiveCur = acc.allActivePeriod.getOrDefault(d, Set.of()).size();
            int activeMesCur = acc.activeMessagesPeriod.getOrDefault(d, Set.of()).size();
            int activeReactCur = acc.activeReactionsPeriod.getOrDefault(d, Set.of()).size();
            int activeGiftsCur = acc.activeGiftsPeriod.getOrDefault(d, Set.of()).size();
            int activeMesAndReactCur = intersection(acc.activeMessagesPeriod.getOrDefault(d, Set.of()),
                    acc.activeReactionsPeriod.getOrDefault(d, Set.of())).size();
            int publicUserCur = acc.publicationsUserPeriod.getOrDefault(d, 0);
            int publicAdminCur = acc.publicationsAdminPeriod.getOrDefault(d, 0);
            int publicAllCur = acc.publicationsUserOrAdminPeriod.getOrDefault(d, 0);

            innerAllActiveUsersPeriod.add(Map.entry(d, allActiveCur));
            innerActiveUsersPerCommentsPeriod.add(Map.entry(d, activeMesCur));
            innerActiveUsersPerReactionsPeriod.add(Map.entry(d, activeReactCur));
            innerActiveUsersPerGiftsPeriod.add(Map.entry(d, activeGiftsCur));
            innerActiveUsersPerCommAndReactPeriod.add(Map.entry(d, activeMesAndReactCur));
            innerPublicationsFromAdminPeriod.add(Map.entry(d, publicAdminCur));
            innerPublicationsFromUserPeriod.add(Map.entry(d, publicUserCur));
            innerAllPublicationsPeriod.add(Map.entry(d, publicAllCur));
        }

        double au = analyticCoreService.au((long) acc.allActive.size(), days);
        double reactionsPerPost = analyticCoreService.reactionsPerPost(
                acc.totalReactions,
                (long) acc.totalPublicAdmin);
        double commentsPerPost = analyticCoreService.commentsPerPost(
                (long) acc.totalPublicUser,
                (long) acc.totalPublicAdmin);
        double engagementRate = analyticCoreService.engagementRate(au,
                reactionsPerPost,
                commentsPerPost);

        long forWauUniqActions = 0;
        List<Map.Entry<LocalDate, Double>> innerReactionsPerPostPeriod = new ArrayList<>();
        List<Map.Entry<LocalDate, Double>> innerCommentsPerPostPeriod = new ArrayList<>();
        List<Map.Entry<LocalDate, Double>> innerEngagementRatePeriod = new ArrayList<>();
        List<Map.Entry<LocalDate, Double>> innerStickinessRatioPeriod = new ArrayList<>();

        List<Double> innerUsageRegularityPeriod = new ArrayList<>();

        for (LocalDate d = fromLD; !d.isAfter(toLD); d = d.plusDays(1)) {

            int dau = acc.allActivePeriod.getOrDefault(d, Set.of()).size();
            forWauUniqActions += dau;
            double sr = analyticCoreService.stickinessRatio(dau, au);
            double commentsPerPostD = analyticCoreService.commentsPerPost(
                    (long) acc.publicationsUserPeriod.getOrDefault(d, 0),
                    (long) acc.publicationsAdminPeriod.getOrDefault(d, 0));
            double reactionsPerPostD = analyticCoreService.reactionsPerPost(
                    acc.reactionsPeriod.getOrDefault(d, 0L),
                    (long) acc.publicationsAdminPeriod.getOrDefault(d, 0));
            double engagementRateD = analyticCoreService.engagementRate(dau,
                    reactionsPerPostD, commentsPerPostD);
            innerCommentsPerPostPeriod
                    .add(Map.entry(d, commentsPerPostD));
            innerReactionsPerPostPeriod
                    .add(Map.entry(d, reactionsPerPostD));
            innerEngagementRatePeriod
                    .add(Map.entry(d, engagementRateD));
            innerStickinessRatioPeriod
                    .add(Map.entry(d, sr));
            long daysBetween = Period.between(fromLD, d).getDays();
            if (daysBetween > 0 && PeriodType.WEEKLY.isPeriodBoundary(daysBetween)) {
                double wau = analyticCoreService.au(forWauUniqActions,
                        PeriodType.WEEKLY.getDays());
                innerUsageRegularityPeriod
                        .add(analyticCoreService.usageRegularity(wau, au));
                forWauUniqActions = 0;
            }
        }

        return new BaseMetricsDto(allActiveUsers, innerAllActiveUsersPeriod, activeUsersPerComments,
                innerActiveUsersPerCommentsPeriod, activeUsersPerReactions, innerActiveUsersPerReactionsPeriod,
                activeUsersPerGifts, innerActiveUsersPerGiftsPeriod, activeUsersPerCommentsAndReactions,
                innerActiveUsersPerCommAndReactPeriod, allPublications,
                innerAllPublicationsPeriod, publicationsFromAdmin, innerPublicationsFromAdminPeriod,
                publicationsFromUser,
                innerPublicationsFromUserPeriod, reactionsPerPost, innerReactionsPerPostPeriod, commentsPerPost,
                innerCommentsPerPostPeriod,
                engagementRate, innerEngagementRatePeriod, innerParticipantChanged, innerStickinessRatioPeriod,
                innerUsageRegularityPeriod);
    }

    private Set<String> intersection(Set<String> one, Set<String> two) {
        Set<String> intersection = new HashSet<>(one);
        intersection.retainAll(two);
        return intersection;
    }
}