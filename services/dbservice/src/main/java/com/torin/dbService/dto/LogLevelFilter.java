package com.torin.dbService.dto;

import java.util.List;

public enum LogLevelFilter {
    INFO,
    WARNING,
    ERROR,
    FATALERROR;

    public List<String> esLevels() {
        return switch (this) {
            case INFO -> List.of("Info", "Warning", "Error", "FatalError");
            case WARNING -> List.of("Warning", "Error", "FatalError");
            case ERROR -> List.of("Error", "FatalError");
            case FATALERROR -> List.of("FatalError");
        };
    }
}
