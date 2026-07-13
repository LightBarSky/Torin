package com.torin.analytic.infrastructure;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.torin.core.dto.ActivityEntityDto;

public class ActivityBaseUserAccumulator {
    public Map<Long, ActivityEntityDto> activityByGroup = new HashMap<>();
    public Map<LocalDate, ActivityEntityDto> activityPeriod = new HashMap<>();

    public ActivityEntityDto total = new ActivityEntityDto();

    public void add(ActivityEventBaseUser e) {
        var activityEntity = activityByGroup.computeIfAbsent(e.groupId(), x -> new ActivityEntityDto());
        activityEntity.addMessages(e.messages());
        activityEntity.addReactions(e.reactions());
        activityEntity.addGifts(e.gifts());

        total.addMessages(e.messages());
        total.addReactions(e.reactions());
        total.addGifts(e.gifts());

        if (e.date() != null) {
            var activityEntityPeriod = activityPeriod.computeIfAbsent(e.date(), x -> new ActivityEntityDto());
            activityEntityPeriod.addMessages(e.messages());
            activityEntityPeriod.addReactions(e.reactions());
            activityEntityPeriod.addGifts(e.gifts());
        }
    }
}
