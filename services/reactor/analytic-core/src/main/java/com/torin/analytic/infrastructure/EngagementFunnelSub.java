package com.torin.analytic.infrastructure;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngagementFunnelSub {
    private Long idMessage;
    private Long replies = 0L;
    private Long views = 0L;
    private LocalDate date;

    public void setReplies(Long replies) {
        this.replies = Math.max(this.replies, replies);
    }

    public void setViews(Long views) {
        this.views = Math.max(this.views, views);
    }

    public void setIdMessage(Long idMessage) {
        this.idMessage = Math.min(this.idMessage, idMessage);
    }
}
