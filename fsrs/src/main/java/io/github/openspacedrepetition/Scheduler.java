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
}
