/* (C)2025 */
package io.github.openspacedrepetition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@ToString
@EqualsAndHashCode(exclude = "randomSeed")
@JsonDeserialize(builder = Scheduler.Builder.class)
public class Scheduler {

    // constants
    private static final double[] DEFAULT_PARAMETERS = {
        0.2172, 1.1771, 3.2602, 16.1507, 7.0114, 0.57, 2.0966, 0.0069, 1.5261, 0.112, 1.0178, 1.849,
        0.1133, 0.3127, 2.2934, 0.2191, 3.0004, 0.7536, 0.3332, 0.1437, 0.2
    };
    private static final double DEFUALT_DESIRED_RETENTION = 0.9;
    private static final Duration[] DEFAULT_LEARNING_STEPS = {
        Duration.ofMinutes(1), Duration.ofMinutes(10)
    };
    private static final Duration[] DEFAULT_RELEARNING_STEPS = {Duration.ofMinutes(10)};
    private static final int DEFAULT_MAXIMUM_INTERVAL = 36500;
    private static final boolean DEFAULT_ENABLE_FUZZING = true;
    private static final int DEFAULT_RANDOM_SEED_NUMBER = 42;
    public static final double STABILITY_MIN = 0.001;
    private static final double MIN_DIFFICULTY = 1.0;
    private static final double MAX_DIFFICULTY = 10.0;

    private static record FuzzRange(double start, double end, double factor) {}

    private static final FuzzRange[] FUZZ_RANGES = {
        new FuzzRange(2.5, 7.0, 0.15),
        new FuzzRange(7.0, 20.0, 0.1),
        new FuzzRange(20.0, Double.POSITIVE_INFINITY, 0.05),
    };

    // configurable instance variables
    private final double[] parameters;
    private final double desiredRetention;
    private final Duration[] learningSteps;
    private final Duration[] relearningSteps;
    private final int maximumInterval;
    private final boolean enableFuzzing;
    private final int randomSeedNumber;

    // derived instance variables
    private final double DECAY;
    private final double FACTOR;
    @JsonIgnore private final Random randomSeed;

    private Scheduler(@NonNull Builder builder) {

        this.parameters = builder.parameters;
        this.desiredRetention = builder.desiredRetention;
        this.learningSteps = builder.learningSteps;
        this.relearningSteps = builder.relearningSteps;
        this.maximumInterval = builder.maximumInterval;
        this.enableFuzzing = builder.enableFuzzing;
        this.randomSeedNumber = builder.randomSeedNumber;

        this.DECAY = -this.parameters[20];
        this.FACTOR = Math.pow(0.9, 1.0 / this.DECAY) - 1;
        this.randomSeed = new Random(this.randomSeedNumber);
    }

    @JsonIgnore
    public double getDECAY() {
        return this.DECAY;
    }

    @JsonIgnore
    public double getFACTOR() {
        return this.FACTOR;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Setter
    @Accessors(fluent = true, chain = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private double[] parameters = DEFAULT_PARAMETERS;
        private double desiredRetention = DEFUALT_DESIRED_RETENTION;
        private Duration[] learningSteps = DEFAULT_LEARNING_STEPS;
        private Duration[] relearningSteps = DEFAULT_RELEARNING_STEPS;
        private int maximumInterval = DEFAULT_MAXIMUM_INTERVAL;
        private boolean enableFuzzing = DEFAULT_ENABLE_FUZZING;
        private int randomSeedNumber = DEFAULT_RANDOM_SEED_NUMBER;

        public Scheduler build() {
            return new Scheduler(this);
        }
    }

    public Scheduler(@NonNull Scheduler otherScheduler) {

        this.parameters = otherScheduler.parameters;
        this.desiredRetention = otherScheduler.desiredRetention;
        this.learningSteps = otherScheduler.learningSteps;
        this.relearningSteps = otherScheduler.relearningSteps;
        this.maximumInterval = otherScheduler.maximumInterval;
        this.enableFuzzing = otherScheduler.enableFuzzing;
        this.randomSeedNumber = otherScheduler.randomSeedNumber;
        this.DECAY = otherScheduler.DECAY;
        this.FACTOR = otherScheduler.FACTOR;
        this.randomSeed = otherScheduler.randomSeed;
    }

    public String toJson() {

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Scheduler fromJson(@NonNull String json) {

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try {
            return mapper.readValue(json, Scheduler.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public double getCardRetrievability(@NonNull Card card, @NonNull Instant currentDatetime) {

        if (card.getLastReview() == null) {
            return 0;
        }

        int elapsedDays =
                (int) Math.max(0, ChronoUnit.DAYS.between(card.getLastReview(), currentDatetime));

        return Math.pow(1 + this.FACTOR * elapsedDays / card.getStability(), this.DECAY);
    }

    public double getCardRetrievability(@NonNull Card card) {

        return getCardRetrievability(card, Instant.now());

    }

    private double clampStability(double stability) {

        return Math.max(stability, STABILITY_MIN);
    }

    private double initialStability(@NonNull Rating rating) {

        double initialStability = this.parameters[rating.getValue() - 1];

        initialStability = clampStability(initialStability);

        return initialStability;
    }

    private double clampDifficulty(double difficulty) {

        return Math.min(Math.max(difficulty, MIN_DIFFICULTY), MAX_DIFFICULTY);
    }

    private double initialDifficulty(@NonNull Rating rating) {

        double initialDifficulty =
                this.parameters[4]
                        - Math.pow(Math.E, (this.parameters[5] * (rating.getValue() - 1)))
                        + 1;

        initialDifficulty = clampDifficulty(initialDifficulty);

        return initialDifficulty;
    }

    private double shortTermStability(double stability, @NonNull Rating rating) {

        double shortTermStabilityIncrease =
                Math.exp(this.parameters[17] * (rating.getValue() - 3 + this.parameters[18]))
                        * Math.pow(stability, -this.parameters[19]);

        if (rating == Rating.GOOD || rating == Rating.EASY) {

            shortTermStabilityIncrease = Math.max(shortTermStabilityIncrease, 1.0);
        }

        double shortTermStability = stability * shortTermStabilityIncrease;

        shortTermStability = clampStability(shortTermStability);

        return shortTermStability;
    }

    private double linearDamping(double deltaDifficulty, double difficulty) {

        return (10.0 - difficulty) * deltaDifficulty / 9.0;
    }

    private double meanReversion(double arg1, double arg2) {

        return this.parameters[7] * arg1 + (1 - this.parameters[7]) * arg2;
    }

    private double nextDifficulty(double difficulty, @NonNull Rating rating) {

        double arg1 = initialDifficulty(Rating.EASY);

        double deltaDifficulty = -(this.parameters[6] * (rating.getValue() - 3));

        double arg2 = difficulty + linearDamping(deltaDifficulty, difficulty);

        double nextDifficulty = meanReversion(arg1, arg2);

        nextDifficulty = clampDifficulty(nextDifficulty);

        return nextDifficulty;
    }

    private double nextForgetStability(double difficulty, double stability, double retrievability) {

        double nextForgetStabilityLongTermParams =
                this.parameters[11]
                        * (Math.pow(difficulty, -this.parameters[12]))
                        * (Math.pow(stability + 1, this.parameters[13]) - 1)
                        * Math.exp((1 - retrievability) * this.parameters[14]);

        double nextForgetStabilityShortTermParams =
                stability / Math.exp(this.parameters[17] * this.parameters[18]);

        return Math.min(nextForgetStabilityLongTermParams, nextForgetStabilityShortTermParams);
    }

    private double nextRecallStability(
            double difficulty, double stability, double retrievability, @NonNull Rating rating) {

        double hardPenalty = (rating == Rating.HARD) ? this.parameters[15] : 1;

        double easyBonus = (rating == Rating.EASY) ? this.parameters[16] : 1;

        return stability
                * (1
                        + Math.exp(this.parameters[8])
                                * (11 - difficulty)
                                * Math.pow(stability, -this.parameters[9])
                                * (Math.exp((1 - retrievability) * this.parameters[10]) - 1)
                                * hardPenalty
                                * easyBonus);
    }

    private double nextStability(
            double difficulty, double stability, double retrievability, @NonNull Rating rating) {

        double nextStability;

        if (rating == Rating.AGAIN) {

            nextStability = nextForgetStability(difficulty, stability, retrievability);

        } else { // HARD || GOOD || EASY

            nextStability = nextRecallStability(difficulty, stability, retrievability, rating);
        }

        nextStability = clampStability(nextStability);

        return nextStability;
    }

    private int nextInterval(double stability) {

        int nextInterval =
                (int)
                        Math.round(
                                (stability / this.FACTOR)
                                        * (Math.pow(this.desiredRetention, (1 / this.DECAY)) - 1));

        // must be at least 1 day long
        nextInterval = Math.max(nextInterval, 1);

        // cannot be longer than the maximum interval
        nextInterval = Math.min(nextInterval, this.maximumInterval);

        return nextInterval;
    }

    private int[] getFuzzRange(int intervalDays) {

        double delta = 1.0;
        for (FuzzRange fuzzRange : FUZZ_RANGES) {

            delta +=
                    fuzzRange.factor()
                            * Math.max(
                                    Math.min(intervalDays, fuzzRange.end() - fuzzRange.start()),
                                    0.0);
        }

        int minIvl = (int) Math.round(intervalDays - delta);
        int maxIvl = (int) Math.round(intervalDays + delta);

        // make sure the minIvl and maxIvl fall into a valid range
        minIvl = Math.max(2, minIvl);
        maxIvl = Math.min(maxIvl, this.maximumInterval);
        minIvl = Math.min(minIvl, maxIvl);

        return new int[] {minIvl, maxIvl};
    }

    private Duration getFuzzedInterval(@NonNull Duration interval) {

        int intervalDays = (int) interval.toDays();

        if (intervalDays < 2.5) {
            return interval;
        }

        int[] ivlBounds = getFuzzRange(intervalDays);

        int minIvl = ivlBounds[0];
        int maxIvl = ivlBounds[1];

        double fuzzedIntervalDaysDouble =
                (randomSeed.nextDouble() * (maxIvl - minIvl + 1)) + minIvl;

        int fuzzedIntervalDays =
                Math.min((int) Math.round(fuzzedIntervalDaysDouble), this.maximumInterval);

        return Duration.ofDays(fuzzedIntervalDays);
    }

    public CardAndReviewLog reviewCard(
            Card card, Rating rating, Instant reviewDatetime, Integer reviewDuration) {

        card = new Card(card);

        if (reviewDatetime == null) {
            reviewDatetime = Instant.now();
        }

        Integer daysSinceLastReview;

        Duration nextInterval = Duration.ofMillis(0); // this is just a temporary initialization

        if (card.getLastReview() == null) {
            daysSinceLastReview = null;
        } else {
            daysSinceLastReview =
                    (int) ChronoUnit.DAYS.between(card.getLastReview(), reviewDatetime);
        }

        State cardState = card.getState();
        switch (cardState) {
            case LEARNING -> {
                if (card.getStability() == null && card.getDifficulty() == null) {

                    double initialStability = initialStability(rating);
                    double initialDifficulty = initialDifficulty(rating);

                    card.setStability(initialStability);
                    card.setDifficulty(initialDifficulty);

                } else if (daysSinceLastReview != null && daysSinceLastReview < 1) {

                    double shortTermStability = shortTermStability(card.getStability(), rating);
                    double nextDifficulty = nextDifficulty(card.getDifficulty(), rating);

                    card.setStability(shortTermStability);
                    card.setDifficulty(nextDifficulty);

                } else {

                    double retrievability = getCardRetrievability(card, reviewDatetime);

                    double nextStability =
                            nextStability(
                                    card.getDifficulty(),
                                    card.getStability(),
                                    retrievability,
                                    rating);
                    double nextDifficulty = nextDifficulty(card.getDifficulty(), rating);

                    card.setStability(nextStability);
                    card.setDifficulty(nextDifficulty);
                }

                /*
                calculate the card's next interval
                first if-clause handles edge case where the Card in the Learning state was previously
                scheduled with a Scheduler with more learning_steps than the current Scheduler
                */
                int nextIntervalDays;
                if (this.learningSteps.length == 0
                        || (card.getStep() >= this.learningSteps.length
                                && (rating == Rating.HARD
                                        || rating == Rating.GOOD
                                        || rating == Rating.EASY))) {

                    card.setState(State.REVIEW);
                    card.setStep(null);

                    nextIntervalDays = nextInterval(card.getStability());
                    nextInterval = Duration.ofDays((long) nextIntervalDays);

                } else {

                    switch (rating) {
                        case AGAIN -> {
                            card.setStep(0);
                            nextInterval = this.learningSteps[card.getStep()];
                        }
                        case HARD -> {

                            // card step stays the same

                            if (card.getStep() == 0 && this.learningSteps.length == 1) {

                                nextInterval =
                                        Duration.ofMillis(
                                                Math.round(this.learningSteps[0].toMillis() * 1.5));

                            } else if (card.getStep() == 0) {

                                nextInterval =
                                        Duration.ofMillis(
                                                Math.round(
                                                        (this.learningSteps[0].toMillis()
                                                                        + this.learningSteps[1]
                                                                                .toMillis())
                                                                / 2.0));

                            } else {

                                nextInterval = this.learningSteps[card.getStep()];
                            }
                        }
                        case GOOD -> {
                            if (card.getStep() + 1 == this.learningSteps.length) {

                                // the last step

                                card.setState(State.REVIEW);
                                card.setStep(null);

                                nextIntervalDays = nextInterval(card.getStability());
                                nextInterval = Duration.ofDays((long) nextIntervalDays);

                            } else {

                                card.setStep(card.getStep() + 1);
                                nextInterval = this.learningSteps[card.getStep()];
                            }
                        }
                        case EASY -> {
                            card.setState(State.REVIEW);
                            card.setStep(null);

                            nextIntervalDays = nextInterval(card.getStability());
                            nextInterval = Duration.ofDays((long) nextIntervalDays);
                        }
                    }
                }
            }
            case REVIEW -> {

                // update the card's stability and difficulty
                if (daysSinceLastReview != null && daysSinceLastReview < 1) {

                    double shortTermStability = shortTermStability(card.getStability(), rating);

                    card.setStability(shortTermStability);

                } else {

                    double nextStability =
                            nextStability(
                                    card.getDifficulty(),
                                    card.getStability(),
                                    getCardRetrievability(card, reviewDatetime),
                                    rating);

                    card.setStability(nextStability);
                }

                double nextDifficulty = nextDifficulty(card.getDifficulty(), rating);
                card.setDifficulty(nextDifficulty);

                int nextIntervalDays;
                switch (rating) {
                    case AGAIN -> {
                        if (this.relearningSteps.length == 0) {

                            nextIntervalDays = nextInterval(card.getStability());
                            nextInterval = Duration.ofDays(nextIntervalDays);

                        } else {

                            card.setState(State.RELEARNING);
                            card.setStep(0);

                            nextInterval = this.relearningSteps[card.getStep()];
                        }
                    }
                    case HARD, GOOD, EASY -> {
                        nextIntervalDays = nextInterval(card.getStability());
                        nextInterval = Duration.ofDays(nextIntervalDays);
                    }
                }
            }
            case RELEARNING -> {

                // update the card's stability and difficulty
                if (daysSinceLastReview != null && daysSinceLastReview < 1) {

                    double shortTermStability = shortTermStability(card.getStability(), rating);

                    card.setStability(shortTermStability);
                } else {

                    double nextStability =
                            nextStability(
                                    card.getDifficulty(),
                                    card.getStability(),
                                    getCardRetrievability(card, reviewDatetime),
                                    rating);

                    card.setStability(nextStability);
                }

                double nextDifficulty = nextDifficulty(card.getDifficulty(), rating);
                card.setDifficulty(nextDifficulty);

                /*
                calculate the card's next interval
                first if-clause handles edge case where the Card in the Relearning state was previously
                scheduled with a Scheduler with more relearning_steps than the current Scheduler
                */
                int nextIntervalDays;
                if (this.relearningSteps.length == 0
                        || (card.getStep() >= this.relearningSteps.length
                                && (rating == Rating.HARD
                                        || rating == Rating.GOOD
                                        || rating == Rating.EASY))) {

                    card.setState(State.REVIEW);
                    card.setStep(null);

                    nextIntervalDays = nextInterval(card.getStability());
                    nextInterval = Duration.ofDays(nextIntervalDays);

                } else {

                    switch (rating) {
                        case AGAIN -> {
                            card.setStep(0);
                            nextInterval = this.relearningSteps[card.getStep()];
                        }
                        case HARD -> {
                            // card step stays the same

                            if (card.getStep() == 0 && this.relearningSteps.length == 1) {

                                nextInterval =
                                        Duration.ofMillis(
                                                Math.round(this.learningSteps[0].toMillis() * 1.5));

                            } else if (card.getStep() == 0) {

                                nextInterval =
                                        Duration.ofMillis(
                                                Math.round(
                                                        (this.learningSteps[0].toMillis()
                                                                        + this.learningSteps[1]
                                                                                .toMillis())
                                                                / 2.0));

                            } else {

                                nextInterval = this.relearningSteps[card.getStep()];
                            }
                        }
                        case GOOD -> {
                            if (card.getStep() + 1 == this.relearningSteps.length) {

                                card.setState(State.REVIEW);
                                card.setStep(null);

                                nextIntervalDays = nextInterval(card.getStability());
                                nextInterval = Duration.ofDays(nextIntervalDays);

                            } else {

                                card.setStep(card.getStep() + 1);
                                nextInterval = this.relearningSteps[card.getStep()];
                            }
                        }
                        case EASY -> {
                            card.setState(State.REVIEW);
                            card.setStep(null);

                            nextIntervalDays = nextInterval(card.getStability());
                            nextInterval = Duration.ofDays(nextIntervalDays);
                        }
                    }
                }
            }
        }

        if (this.enableFuzzing && card.getState() == State.REVIEW) {

            nextInterval = getFuzzedInterval(nextInterval);
        }

        card.setDue(reviewDatetime.plus(nextInterval));
        card.setLastReview(reviewDatetime);

        ReviewLog reviewLog =
                new ReviewLog(card.getCardId(), rating, reviewDatetime, reviewDuration);

        return new CardAndReviewLog(card, reviewLog);
    }

    public CardAndReviewLog reviewCard(Card card, Rating rating) {

        return reviewCard(card, rating, null, null);
    }

    public CardAndReviewLog reviewCard(Card card, Rating rating, Instant reviewDatetime) {

        return reviewCard(card, rating, reviewDatetime, null);
    }

    public CardAndReviewLog reviewCard(Card card, Rating rating, Integer reviewDuration) {

        return reviewCard(card, rating, null, reviewDuration);
    }
}
