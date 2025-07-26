/* (C)2025 */
package io.github.openspacedrepetition;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.*;

public class FSRSTest {

    @Test
    public void testReviewDefaultArg() {

        Scheduler scheduler = Scheduler.builder().build();

        Card card = Card.builder().build();

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

        Scheduler scheduler = Scheduler.builder().maximumInterval(maximumInterval).build();

        Card card = new Card.Builder().build();

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

        Scheduler scheduler = Scheduler.builder().enableFuzzing(false).build();

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

        Card card = Card.builder().build();
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

        Scheduler scheduler = Scheduler.builder().randomSeed(randomSeed1).build();

        Card card = Card.builder().build();

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

        scheduler = Scheduler.builder().randomSeed(randomSeed2).build();

        card = Card.builder().build();

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

        Scheduler scheduler1 = Scheduler.builder().build();
        Scheduler scheduler2 = Scheduler.builder().desiredRetention(0.91).build();
        Scheduler scheduler1Copy = new Scheduler(scheduler1);

        assertThat(scheduler1).isNotEqualTo(scheduler2);
        assertThat(scheduler1).isEqualTo(scheduler1Copy);

        Card cardOrig = Card.builder().build();
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

    @Test
    public void testUniqueCardIds() {

        List<Integer> cardIds = new ArrayList<Integer>();

        Card card;
        int cardId;
        for (int i = 1; i < 1000; i++) {

            card = Card.builder().build();
            cardId = card.getCardId();
            cardIds.add(cardId);
        }

        int totalCountCardIds = cardIds.size();
        int uniqueCountCardIds = new HashSet<>(cardIds).size();

        assertThat(uniqueCountCardIds).isEqualTo(totalCountCardIds);
    }

    @Test
    public void testReviewLogSerialize() {

        Scheduler scheduler = Scheduler.builder().build();
        Card card = Card.builder().build();

        Rating rating = Rating.AGAIN;
        CardAndReviewLog result = scheduler.reviewCard(card, rating);
        card = result.card();
        ReviewLog reviewLog = result.reviewLog();

        assertThat(reviewLog.toJson()).isInstanceOf(String.class);

        // we can reconstruct a copy of the ReviewLog object equivalent to the original
        String reviewLogJson = reviewLog.toJson();
        ReviewLog copiedReviewLog = ReviewLog.fromJson(reviewLogJson);
        assertThat(reviewLog).isEqualTo(copiedReviewLog);
        assertThat(reviewLog.toJson()).isEqualTo(copiedReviewLog.toJson());

        // (x2) perform the above tests once more with a ReviewLog from a reviewed Card
        rating = Rating.GOOD;
        result = scheduler.reviewCard(card, rating);
        ReviewLog nextReviewLog = result.reviewLog();

        assertThat(reviewLog.toJson()).isInstanceOf(String.class);

        String nextReviewLogJson = nextReviewLog.toJson();
        ReviewLog copiedNextReviewLog = ReviewLog.fromJson(nextReviewLogJson);
        assertThat(nextReviewLog).isEqualTo(copiedNextReviewLog);
        assertThat(nextReviewLog.toJson()).isEqualTo(copiedNextReviewLog.toJson());

        // original review log and next review log are different
        assertThat(reviewLog).isNotEqualTo(nextReviewLog);
        assertThat(reviewLog.toJson()).isNotEqualTo(nextReviewLog.toJson());
    }

    @Test
    public void testCardSerialize() {

        Scheduler scheduler = Scheduler.builder().build();

        Card card = Card.builder().build();

        assertThat(card.toJson()).isInstanceOf(String.class);

        String cardJson = card.toJson();
        Card copiedCard = Card.fromJson(cardJson);
        assertThat(card).isEqualTo(copiedCard);
        assertThat(card.toJson()).isEqualTo(copiedCard.toJson());

        CardAndReviewLog result = scheduler.reviewCard(card, Rating.GOOD);
        Card reviewedCard = result.card();
        ReviewLog reviewLog = result.reviewLog();

        assertThat(reviewedCard.toJson()).isInstanceOf(String.class);

        String reviewedCardJson = reviewedCard.toJson();
        Card copiedReviewedCard = Card.fromJson(reviewedCardJson);
        assertThat(reviewedCard).isEqualTo(copiedReviewedCard);
        assertThat(reviewedCard.toJson()).isEqualTo(copiedReviewedCard.toJson());


        // original card and reviewed card are different
        assertThat(card).isNotEqualTo(reviewedCard);
        assertThat(card.toJson()).isNotEqualTo(reviewedCard.toJson());

    }
}
