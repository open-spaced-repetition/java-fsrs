/* (C)2025 */
package io.github.openspacedrepetition;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
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

        assertTrue(timeDeltaSeconds > 500); // due in approx. 8-10 minutes
    }
}
