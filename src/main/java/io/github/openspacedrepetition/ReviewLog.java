/* (C)2025 */
package io.github.openspacedrepetition;

import java.time.Instant;

public class ReviewLog {

    private int cardId;
    private Rating rating;
    private Instant reviewDatetime;
    private Integer reviewDuration;

    public ReviewLog(int cardId, Rating rating, Instant reviewDatetime, Integer reviewDuration) {

        this.cardId = cardId;
        this.rating = rating;
        this.reviewDatetime = reviewDatetime;
        this.reviewDuration = reviewDuration;
    }

    public int getCardId() {
        return this.cardId;
    }

    public Rating getRating() {
        return this.rating;
    }

    public Instant getReviewDateTime() {
        return this.reviewDatetime;
    }

    public Integer getReviewDuration() {
        return this.reviewDuration;
    }
}
