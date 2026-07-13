package com.torin.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityEntityDto {
    private int allMessages = 0;
    private int allReactions = 0;
    private int allGifts = 0;

    public void addMessages(int message) {
        this.allMessages += message;
    }

    public void addReactions(int reactions) {
        this.allReactions += reactions;
    }

    public void addGifts(int gifts) {
        this.allGifts += gifts;
    }
}
