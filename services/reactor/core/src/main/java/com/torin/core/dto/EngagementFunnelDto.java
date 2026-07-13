package com.torin.core.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record EngagementFunnelDto(Double allViewRate, Double allReactionRate, Double allCommentRate,
        Double allERview,
        List<Map.Entry<Long, Double>> viewRatePeriod,
        List<Map.Entry<Long, Double>> reactionRatePeriod,
        List<Map.Entry<Long, Double>> commentRatePeriod,
        List<Map.Entry<Long, Double>> ERviewPeriod,
        List<Map.Entry<LocalDate, EngagementFunnelPerDayDto>> ERperDay) {
}