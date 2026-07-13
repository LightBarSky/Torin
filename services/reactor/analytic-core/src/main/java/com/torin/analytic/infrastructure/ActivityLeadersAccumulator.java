package com.torin.analytic.infrastructure;

import java.util.HashMap;
import java.util.Map;

public class ActivityLeadersAccumulator {
    public Map<String, Long> allActivity = new HashMap<>();
    public Map<String, Long> activityMessages = new HashMap<>();
    public Map<String, Long> activityReactions = new HashMap<>();

    public void add(ActivityEventAQandAL e) {
        if (e.idFrom() != null) {
            allActivity.merge(e.idFrom(), 1L, (a, b) -> a + b);
            switch (e.typeEvent()) {
                case TypeEvent.MESSAGE -> activityMessages.merge(e.idFrom(), 1L, (a, b) -> a + b);
                case TypeEvent.REACTIONS -> activityReactions.merge(e.idFrom(), 1L, (a, b) -> a + b);
                default -> {
                }
            }
        }
    }
}
