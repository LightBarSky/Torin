package com.torin.analytic.infrastructure;

public enum PeriodType {
    DAILY(1),
    WEEKLY(7),
    MONTHLY(30);

    private final long days;

    PeriodType(long days) {
        this.days = days;
    }

    public long getDays() {
        return days;
    }

    public boolean isPeriodBoundary(long daysBetween) {
        return daysBetween % days == 0;
    }
}
