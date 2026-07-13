package com.torin.analytic.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsForEngagement {
    private Long replies;
    private Long views;
    private Long reactions;
    private Long posts;
}
