/* (C)2025 */
package io.github.openspacedrepetition;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Scheduler {

    private static final double[] DEFAULT_PARAMETERS = {
        0.2172, 1.1771, 3.2602, 16.1507, 7.0114, 0.57, 2.0966, 0.0069, 1.5261, 0.112, 1.0178, 1.849,
        0.1133, 0.3127, 2.2934, 0.2191, 3.0004, 0.7536, 0.3332, 0.1437, 0.2
    };
    private static final double STABILITY_MIN = 0.001;
    private static final double INITIAL_STABILITY_MAX = 100.0;
    private static final double MIN_DIFFICULTY = 1.0;
    private static final double MAX_DIFFICULTY = 10.0;
    private final double DECAY;
    private final double FACTOR;

    private double[] parameters;
    private double desiredRetention;
    private Duration[] learningSteps;
    private Duration[] relearningSteps;
    private int maximumInterval;
    private boolean enableFuzzing;

    public Scheduler(
            double[] parameters,
            double desiredRetention,
            Duration[] learningSteps,
            Duration[] relearningSteps,
            int maximumInterval,
            boolean enableFuzzing) {

        if (parameters == null) {
            parameters = DEFAULT_PARAMETERS;
        }
        this.parameters = parameters;

        this.desiredRetention = desiredRetention;
        this.learningSteps = learningSteps;
        this.relearningSteps = relearningSteps;
        this.maximumInterval = maximumInterval;
        this.enableFuzzing = enableFuzzing;

        this.DECAY = -this.parameters[20];
        this.FACTOR = Math.pow(0.9, 1.0 / this.DECAY) - 1;
    }

    public double getCardRetrievability(Card card, Instant currentDatetime) {

        Instant cardLastReview = card.getLastReview();
        Double cardStability = card.getStability();

        if (cardLastReview == null) {
            return 0;
        }

        if (currentDatetime == null) {
            currentDatetime = Instant.now();
        }

        int elapsedDays =
                (int) Math.max(0, ChronoUnit.DAYS.between(cardLastReview, currentDatetime));

        return Math.pow(1 + this.FACTOR * elapsedDays / cardStability, this.DECAY);
    }

    private double clampStability(double stability) {

        return Math.max(stability, STABILITY_MIN);

    }

    private double initialStability(Rating rating) {

        double initialStability = this.parameters[rating.getValue()-1];

        initialStability = clampStability(initialStability);

        return initialStability;

    }

    private double clampDifficulty(double difficulty) {

        return Math.min(Math.max(difficulty, MIN_DIFFICULTY), MAX_DIFFICULTY);

    }

    private double initialDifficulty(Rating rating) {

        double initialDifficulty = this.parameters[4] - Math.pow(Math.E, (this.parameters[5] * (rating.getValue() - 1))) + 1;

        initialDifficulty = clampDifficulty(initialDifficulty);

        return initialDifficulty;

    }

    public CardAndReviewLog reviewCard(Card card, Rating rating, Instant reviewDatetime, Integer reviewDuration) {

        card = new Card(card);
        State cardState = card.getState();
        Double cardStability = card.getStability();
        Double cardDifficulty = card.getDifficulty();

        if (reviewDatetime == null) {
            reviewDatetime = Instant.now();
        }
        
        Instant cardLastReview = card.getLastReview();
        Integer daysSinceLastReview;

        if (cardLastReview == null) {
            daysSinceLastReview = null;
        } else {
            daysSinceLastReview = (int) ChronoUnit.DAYS.between(cardLastReview, reviewDatetime);
        }

        switch (cardState) {

            case LEARNING -> {
                
                if (cardStability == null && cardDifficulty == null) {

                    double initialStability = initialStability(rating);
                    double initialDifficulty = initialDifficulty(rating);

                    card.setStability(initialStability);
                    card.setDifficulty(initialDifficulty);

                } else if (daysSinceLastReview != null && daysSinceLastReview < 1) {

                    // TODO:

                } else {

                    // TODO:

                }

            }
            case REVIEW -> {

                // TODO:

            }
            case RELEARNING -> {

                // TODO:

            }

        }

        return new CardAndReviewLog();
    }
}
