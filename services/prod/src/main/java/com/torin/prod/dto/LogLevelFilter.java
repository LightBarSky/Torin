package com.torin.prod.dto;

public enum LogLevelFilter {
    INFO(1),
    WARNING(2),
    ERROR(3),
    FATALERROR(4);

    private final int priority;

    LogLevelFilter(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }

    public static LogLevelFilter from(String value) {
        if (value == null)
            return LogLevelFilter.INFO;
        try {
            return LogLevelFilter.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return LogLevelFilter.INFO;
        }
    }
}
