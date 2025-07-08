/* (C)2025 */
package io.github.openspacedrepetition;

import static org.junit.jupiter.api.Assertions.*;

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

        Scheduler scheduler = Scheduler.defaultScheduler();

        Card card = new Card();

        Rating rating = Rating.GOOD;

        CardAndReviewLog result = scheduler.reviewCard(card, rating);
        card = result.card();

        Instant due = card.getDue();

        Duration timeDelta = Duration.between(Instant.now(), due);
        int timeDeltaSeconds = (int) timeDelta.toSeconds();

        assertTrue(timeDeltaSeconds > 500); // due in approx. 8-10 minutes
    }

    @Test
    public void testMaximumInterval() {

        int maximumInterval = 100;

        Scheduler scheduler = new Scheduler.Builder().setMaximumInterval(maximumInterval).build();

        Card card = new Card();

        CardAndReviewLog result = scheduler.reviewCard(card, Rating.EASY, card.getDue());
        card = result.card();

        assertTrue(
                Duration.between(card.getLastReview(), card.getDue()).toDays()
                        <= scheduler.getMaximumInterval());

        result = scheduler.reviewCard(card, Rating.GOOD, card.getDue());
        card = result.card();

        assertTrue(
                Duration.between(card.getLastReview(), card.getDue()).toDays()
                        <= scheduler.getMaximumInterval());

        result = scheduler.reviewCard(card, Rating.EASY, card.getDue());
        card = result.card();

        assertTrue(
                Duration.between(card.getLastReview(), card.getDue()).toDays()
                        <= scheduler.getMaximumInterval());

        result = scheduler.reviewCard(card, Rating.GOOD, card.getDue());
        card = result.card();

        assertTrue(
                Duration.between(card.getLastReview(), card.getDue()).toDays()
                        <= scheduler.getMaximumInterval());
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

        assertEquals(List.of(0, 4, 14, 45, 135, 372, 0, 0, 2, 5, 10, 20, 40), ivlHistory);
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

        assertEquals(19, intervalDays);

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

        assertEquals(18, intervalDays);
    }
}
