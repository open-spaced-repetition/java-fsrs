/* (C)2025 */
package io.github.openspacedrepetition;

import java.time.Instant;

enum Rating {
    AGAIN(1),
    HARD(2),
    GOOD(3),
    EASY(4);

    private final int value;

    Rating(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

public class ReviewLog {

    private int cardId;
    private Rating rating;
    private Instant reviewDatetime;
    private int reviewDuration;

    public ReviewLog(int cardId, Rating rating, Instant reviewDatetime, int reviewDuration) {

        this.cardId = cardId;
        this.rating = rating;
        this.reviewDatetime = reviewDatetime;
        this.reviewDuration = reviewDuration;
    }
}
