package com.torin.dbService.dto;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ListenerStatusSubDto {
    @JsonIgnore
    private final AtomicLong countAtomic = new AtomicLong(0);
    private Instant date;

    @JsonProperty("count")
    public long getCountValue() {
        return countAtomic.get();
    }

    public long addAndGet(long val) {
        return countAtomic.addAndGet(val);
    }

    public long getCount() {
        return countAtomic.get();
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public Instant getDate() {
        return date;
    }
}
