/* (C)2025 */
package io.github.openspacedrepetition;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.*;

public class FSRSTest {

    @Test
    public void testReviewDefaultArg() {

        Scheduler scheduler = new Scheduler();

        Card card = new Card();

        Rating rating = Rating.GOOD;

        CardAndReviewLog result = scheduler.reviewCard(card, rating);
        card = result.card();

        Instant due = card.getDue();

        Duration timeDelta = Duration.between(Instant.now(), due);
        int timeDeltaSeconds = (int) timeDelta.toSeconds();

        assertThat(timeDeltaSeconds).isGreaterThan(500); // due in approx. 8-10 minutes
    }

    @Test
    public void testMaximumInterval() {

        int maximumInterval = 100;

        Scheduler scheduler = new Scheduler.Builder().setMaximumInterval(maximumInterval).build();

        Card card = new Card();

        CardAndReviewLog result = scheduler.reviewCard(card, Rating.EASY, card.getDue());
        card = result.card();

        assertThat(Duration.between(card.getLastReview(), card.getDue()).toDays())
                .isLessThanOrEqualTo(scheduler.getMaximumInterval());

        result = scheduler.reviewCard(card, Rating.GOOD, card.getDue());
        card = result.card();
        assertThat(Duration.between(card.getLastReview(), card.getDue()).toDays())
                .isLessThanOrEqualTo(scheduler.getMaximumInterval());

        result = scheduler.reviewCard(card, Rating.EASY, card.getDue());
        card = result.card();
        assertThat(Duration.between(card.getLastReview(), card.getDue()).toDays())
                .isLessThanOrEqualTo(scheduler.getMaximumInterval());

        result = scheduler.reviewCard(card, Rating.GOOD, card.getDue());
        card = result.card();
        assertThat(Duration.between(card.getLastReview(), card.getDue()).toDays())
                .isLessThanOrEqualTo(scheduler.getMaximumInterval());
    }

    @Test
    public void testReviewCard() {

        Scheduler scheduler = new Scheduler.Builder().setEnableFuzzing(false).build();

        Rating[] ratings = {
            Rating.GOOD,
            Rating.GOOD,
            Rating.GOOD,
            Rating.GOOD,
            Rating.GOOD,
            Rating.GOOD,
            Rating.AGAIN,
            Rating.AGAIN,
            Rating.GOOD,
            Rating.GOOD,
            Rating.GOOD,
            Rating.GOOD,
            Rating.GOOD,
        };

        Card card = new Card();
        Instant reviewDatetime = Instant.parse("2022-11-29T12:30:00Z");

        List<Integer> ivlHistory = new ArrayList<>();
        for (Rating rating : ratings) {

            CardAndReviewLog result = scheduler.reviewCard(card, rating, reviewDatetime);
            card = result.card();

            int ivl = (int) ChronoUnit.DAYS.between(card.getLastReview(), card.getDue());
            ivlHistory.add(ivl);

            reviewDatetime = card.getDue();
        }
        assertThat(ivlHistory).isEqualTo(List.of(0, 4, 14, 45, 135, 372, 0, 0, 2, 5, 10, 20, 40));
    }

    @Test
    public void testFuzz() {

        Random randomSeed1 = new Random(42);

        Scheduler scheduler = new Scheduler.Builder().setRandomSeed(randomSeed1).build();

        Card card = new Card();

        CardAndReviewLog result = scheduler.reviewCard(card, Rating.GOOD, Instant.now());
        card = result.card();

        result = scheduler.reviewCard(card, Rating.GOOD, card.getDue());
        card = result.card();

        result = scheduler.reviewCard(card, Rating.GOOD, card.getDue());
        card = result.card();

        Duration interval = Duration.between(card.getLastReview(), card.getDue());
        int intervalDays = (int) interval.toDays();
        assertThat(intervalDays).isEqualTo(19);

        Random randomSeed2 = new Random(12345);

        scheduler = new Scheduler.Builder().setRandomSeed(randomSeed2).build();

        card = new Card();

        result = scheduler.reviewCard(card, Rating.GOOD, Instant.now());
        card = result.card();

        result = scheduler.reviewCard(card, Rating.GOOD, card.getDue());
        card = result.card();

        result = scheduler.reviewCard(card, Rating.GOOD, card.getDue());
        card = result.card();

        interval = Duration.between(card.getLastReview(), card.getDue());
        intervalDays = (int) interval.toDays();
        assertThat(intervalDays).isEqualTo(18);
    }

    @Test
    public void testEqualsMethods() {

        Scheduler scheduler1 = new Scheduler();
        Scheduler scheduler2 = new Scheduler.Builder().setDesiredRetention(0.91).build();
        Scheduler scheduler1Copy = new Scheduler(scheduler1);

        assertThat(scheduler1).isNotEqualTo(scheduler2);
        assertThat(scheduler1).isEqualTo(scheduler1Copy);

        Card cardOrig = new Card();
        Card cardOrigCopy = new Card(cardOrig);

        assertThat(cardOrig).isEqualTo(cardOrigCopy);

        CardAndReviewLog result = scheduler1.reviewCard(cardOrig, Rating.GOOD);
        Card cardReview1 = result.card();
        ReviewLog reviewLogReview1 = result.reviewLog();

        ReviewLog reviewLogReview1Copy = new ReviewLog(reviewLogReview1);

        assertThat(cardOrig).isNotEqualTo(cardReview1);
        assertThat(reviewLogReview1).isEqualTo(reviewLogReview1Copy);

        result = scheduler1.reviewCard(cardReview1, Rating.GOOD);
        ReviewLog reviewLogReview2 = result.reviewLog();

        assertThat(reviewLogReview1).isNotEqualTo(reviewLogReview2);
    }
}
