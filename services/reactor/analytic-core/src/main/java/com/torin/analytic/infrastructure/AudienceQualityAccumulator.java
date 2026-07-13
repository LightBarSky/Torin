package com.torin.analytic.infrastructure;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AudienceQualityAccumulator {
    public Set<String> allActivity = new HashSet<>();
    public Set<String> activityMessages = new HashSet<>();

    public Map<LocalDate, Set<String>> allActivityPeriod = new HashMap<>();
    public Map<LocalDate, Set<String>> activityMessagesPeriod = new HashMap<>();
    public Map<LocalDate, Set<String>> activityReactionsPeriod = new HashMap<>();
    public Map<LocalDate, Set<String>> activityGiftsPeriod = new HashMap<>();

    public Map<LocalDate, Map<Instant, Long>> timeBurstIndexContainer = new HashMap<>();

    public void add(ActivityEventAQandAL e) {
        if (e.idFrom() != null) {
            allActivity.add(e.idFrom());
            if (e.date() != null) {
                allActivityPeriod
                        .computeIfAbsent(e.date().atOffset(ZoneOffset.UTC).toLocalDate(), k -> new HashSet<>())
                        .add(e.idFrom());
            }
            switch (e.typeEvent()) {
                case TypeEvent.MESSAGE -> {
                    activityMessages.add(e.idFrom());
                    if (e.date() != null) {
                        activityMessagesPeriod
                                .computeIfAbsent(e.date().atOffset(ZoneOffset.UTC).toLocalDate(), k -> new HashSet<>())
                                .add(e.idFrom());

                        timeBurstIndexContainer
                                .computeIfAbsent(
                                        e.date().atZone(ZoneOffset.UTC)
                                                .toLocalDate(),
                                        k -> new ConcurrentHashMap<>())
                                .merge(e.date().truncatedTo(
                                        java.time.temporal.ChronoUnit.HOURS),
                                        1L,
                                        (oldVal, newVal) -> oldVal + newVal);
                    }
                }
                case TypeEvent.REACTIONS -> {
                    if (e.date() != null) {
                        activityReactionsPeriod
                                .computeIfAbsent(e.date().atOffset(ZoneOffset.UTC).toLocalDate(), k -> new HashSet<>())
                                .add(e.idFrom());
                    }
                }
                case TypeEvent.GIFTS -> {
                    if (e.date() != null) {
                        activityGiftsPeriod
                                .computeIfAbsent(e.date().atOffset(ZoneOffset.UTC).toLocalDate(), k -> new HashSet<>())
                                .add(e.idFrom());
                    }
                }
                default -> {
                }
            }
        }
    }
}
