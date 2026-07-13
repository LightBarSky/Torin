package com.torin.analytic.infrastructure;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BaseMetricsAccumulator {

    public Map<LocalDate, Set<String>> allActivePeriod = new HashMap<>();
    public Map<LocalDate, Set<String>> activeReactionsPeriod = new HashMap<>();
    public Map<LocalDate, Set<String>> activeMessagesPeriod = new HashMap<>();
    public Map<LocalDate, Set<String>> activeGiftsPeriod = new HashMap<>();

    public Map<LocalDate, Long> reactionsPeriod = new HashMap<>();
    public Map<LocalDate, Long> viewsPeriod = new HashMap<>();

    public Map<LocalDate, Integer> publicationsAdminPeriod = new HashMap<>();
    public Map<LocalDate, Integer> publicationsUserPeriod = new HashMap<>();
    public Map<LocalDate, Integer> publicationsUserOrAdminPeriod = new HashMap<>();

    public long totalReactions = 0;
    public long totalViews = 0;
    public int totalPublicAdmin = 0;
    public int totalPublicUser = 0;
    public int totalPublic = 0;

    public Set<String> allActive = new HashSet<>();
    public Set<String> allActiveReactions = new HashSet<>();
    public Set<String> allActiveGifts = new HashSet<>();
    public Set<String> allActiveMessages = new HashSet<>();

    public void add(ActivityEventBaseMetrics e) {
        if (e.date() == null)
            return;
        LocalDate localDate = e.date().atOffset(ZoneOffset.UTC).toLocalDate();
        if (e.userId() != null) {
            allActivePeriod
                    .computeIfAbsent(localDate, k -> new HashSet<>())
                    .add(e.userId());
            allActive.add(e.userId());

            switch (e.typeEvent()) {
                case TypeEvent.MESSAGE -> {
                    allActiveMessages.add(e.userId());
                    activeMessagesPeriod.computeIfAbsent(localDate, k -> new HashSet<>())
                            .add(e.userId());
                }
                case TypeEvent.REACTIONS -> {
                    allActiveReactions.add(e.userId());
                    activeReactionsPeriod.computeIfAbsent(localDate, k -> new HashSet<>())
                            .add(e.userId());
                }
                case TypeEvent.GIFTS -> {
                    allActiveGifts.add(e.userId());
                    activeGiftsPeriod.computeIfAbsent(localDate, k -> new HashSet<>())
                            .add(e.userId());
                }
                default -> {
                }
            }
        }
        // реакции
        totalReactions += e.reactions();
        reactionsPeriod.merge(localDate, e.reactions(), (a, b) -> a + b);

        // просмотры
        totalViews += e.views();
        viewsPeriod.merge(localDate, e.views(), (a, b) -> a + b);

        // публикации
        totalPublicAdmin += e.publicationsAdmin();
        totalPublicUser += e.publicationsUser();
        totalPublic += (e.publicationsAdmin() + e.publicationsUser());

        publicationsAdminPeriod.merge(localDate, e.publicationsAdmin(), (a, b) -> a + b);
        publicationsUserPeriod.merge(localDate, e.publicationsUser(), (a, b) -> a + b);
        publicationsUserOrAdminPeriod.merge(localDate, e.publicationsUser() + e.publicationsAdmin(), (a, b) -> a + b);
    }
}
