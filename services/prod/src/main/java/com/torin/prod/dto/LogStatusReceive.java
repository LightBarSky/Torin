package com.torin.prod.dto;

public enum LogStatusReceive {
    OLD_MESSAGE(0),
    OLD_MESSAGE_OFF(1),
    NEW_MESSAGE(2);

    private final int status;

    LogStatusReceive(int status) {
        this.status = status;
    }

    public int status() {
        return status;
    }
}
