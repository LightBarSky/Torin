package com.torin.core.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record BaseMetricsDto(Integer allActiveUsers,
        List<Map.Entry<LocalDate, Integer>> allActiveUsersPeriod,

        Integer activeUsersPerComments,
        List<Map.Entry<LocalDate, Integer>> activeUsersPerCommentsPeriod,

        Integer activeUsersPerReactions,
        List<Map.Entry<LocalDate, Integer>> activeUsersPerReactionsPeriod,

        Integer activeUsersPerGifts,
        List<Map.Entry<LocalDate, Integer>> activeUsersPerGiftsPeriod,

        Integer activeUsersPerCommentsAndReactions,
        List<Map.Entry<LocalDate, Integer>> activeUsersPerCommAndReactPeriod,

        // 1.2
        Integer allPublications,
        List<Map.Entry<LocalDate, Integer>> allPublicationsPeriod,

        Integer publicationsFromAdmin,
        List<Map.Entry<LocalDate, Integer>> publicationsFromAdminPeriod,

        Integer publicationsFromUser,
        List<Map.Entry<LocalDate, Integer>> publicationsFromUserPeriod,

        // 1.3
        Double reactionsPerPost,
        List<Map.Entry<LocalDate, Double>> reactionsPerPostPeriod,

        Double commentsPerPost,
        List<Map.Entry<LocalDate, Double>> commentsPerPostPeriod,

        Double engagementRate,
        List<Map.Entry<LocalDate, Double>> engagementRatePeriod,

        // 1.4
        List<Map.Entry<Instant, Long>> participantChanged,

        // 1.5
        List<Map.Entry<LocalDate, Double>> stickinessRatioPeriod,

        // 1.6
        List<Double> usageRegularityPeriod) {
}
