package com.torin.core.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record BaseAnanlyticUserDto(ActivityEntityDto allActivity, List<WordGroupAllDto> groups,
        List<Map.Entry<LocalDate, ActivityEntityDto>> activityPeriod,
        List<Map.Entry<Long, ActivityEntityDto>> activityByGroups) {
}
