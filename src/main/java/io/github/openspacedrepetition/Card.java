/* (C)2025 */
package io.github.openspacedrepetition;

import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Card {

    private final int cardId;

    private State state;
    private Integer step;
    private Double stability;
    private Double difficulty;
    private Instant due;
    private Instant lastReview;

    private Card(Builder builder) {

        this.cardId = builder.cardId;
        this.state = builder.state;
        this.stability = builder.stability;
        this.difficulty = builder.difficulty;
        this.due = builder.due;
        this.lastReview = builder.lastReview;

    }

    public static class Builder {

        private Integer cardId = null;

        private State state = State.LEARNING;
        private Integer step = null;
        private Double stability = null;
        private Double difficulty = null;
        private Instant due = null;
        private Instant lastReview = null;

        public Builder setCardId(int cardId) {
            this.cardId = cardId;
            return this;
        }

        public Builder setState(State state) {
            this.state = state;
            return this;
        }

        public Builder setStep(Integer step) {
            this.step = step;
            return this;
        }

        public Builder setStability(Double stability) {
            this.stability = stability;
            return this;
        }

        public Builder setDifficulty(Double difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder setDue(Instant due) {
            this.due = due;
            return this;
        }

        public Builder setLastReview(Instant lastReview) {
            this.lastReview = lastReview;
            return this;
        }

        public Card build() {

            if (this.cardId == null) {
                this.cardId = (int) Instant.now().toEpochMilli();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // restore interrupted status
                }
            }

            if (this.state == State.LEARNING && this.step == null) {
                this.step = 0;
            }

            if (this.due == null) {
                this.due = Instant.now();
            }


            return new Card(this);
        }


    }

    public Card(Card otherCard) {

        this.cardId = otherCard.cardId;
        this.state = otherCard.state;
        this.step = otherCard.step;
        this.stability = otherCard.stability;
        this.difficulty = otherCard.difficulty;
        this.due = otherCard.due;
        this.lastReview = otherCard.lastReview;
    }
}
