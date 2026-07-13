package com.torin.core.dto;

import java.util.List;
import java.util.Map;

public record ActivityLeadersDto(List<Map.Entry<String, Long>> top10ByMessage,
        List<Map.Entry<String, Long>> top10ByReaction, List<Map.Entry<String, Long>> top10ByMessageAndReaction) {
}