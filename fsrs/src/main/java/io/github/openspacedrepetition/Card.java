/* (C)2025 */
package io.github.openspacedrepetition;

import java.time.Instant;

enum State {
    LEARNING(1),
    REVIEW(2),
    RELEARNING(3);

    private final int value;

    State(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

public class Card {

    private int cardId;
    private State state;
    private Integer step;
    private Double stability;
    private Double difficulty;
    private Instant due;
    private Instant lastReview;

    public Card(
            Integer cardId,
            State state,
            Integer step,
            Double stability,
            Double difficulty,
            Instant due,
            Instant lastReview) {

        if (cardId == null) {
            cardId = (int) Instant.now().toEpochMilli();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restore interrupted status
            }
        }

        this.cardId = cardId;

        this.state = state;

        if (this.state == State.LEARNING && step == null) {
            step = 0;
        }
        this.step = step;

        this.stability = stability;
        this.difficulty = difficulty;

        if (due == null) {
            due = Instant.now();
        }
        this.due = due;

        this.lastReview = lastReview;
    }

    public Card() {

        this(null, State.LEARNING, null, null, null, null, null);
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

    public int getCardId() {
        return this.cardId;
    }

    public Integer getStep() {
        return this.step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Instant getDue() {
        return this.due;
    }

    public void setDue(Instant due) {
        this.due = due;
    }

    public void setLastReview(Instant lastReview) {
        this.lastReview = lastReview;
    }

    public Instant getLastReview() {
        return this.lastReview;
    }

    public Double getStability() {
        return this.stability;
    }

    public void setStability(double stability) {
        this.stability = stability;
    }

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Double getDifficulty() {
        return this.difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }
}
