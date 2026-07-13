package com.torin.core.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record AudienceQualityDto(Double writerToMembersAll, Double writerToShareAll,
        List<Map.Entry<LocalDate, Double>> writerToMembersPeriod,
        List<Map.Entry<LocalDate, Double>> writerToSharePeriod,
        List<Map.Entry<LocalDate, Double>> timeBurstIndexPeriod) {
}
