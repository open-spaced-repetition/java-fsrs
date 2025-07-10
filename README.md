<div align="center">
  <img src="https://raw.githubusercontent.com/open-spaced-repetition/java-fsrs/main/osr_logo.png" height="100" alt="Open Spaced Repetition logo"/>
</div>
<div align="center">

# Java-FSRS

</div>
<div align="center">
  <em>🧠🔄 Build your own Spaced Repetition System in Java 🧠🔄   </em>
</div>
<br />
<div align="center" style="text-decoration: none;">
    <a href="https://central.sonatype.com/artifact/io.github.open-spaced-repetition/fsrs"><img src="https://img.shields.io/maven-central/v/io.github.open-spaced-repetition/fsrs"></a>
    <a href="https://central.sonatype.com/artifact/io.github.open-spaced-repetition/fsrs"><img src="https://img.shields.io/badge/Java-17-blue.svg"></a>
    <a href="https://github.com/open-spaced-repetition/java-fsrs/blob/main/LICENSE" style="text-decoration: none;"><img src="https://img.shields.io/badge/License-MIT-brightgreen.svg"></a>
</div>
<br />

**Java-FSRS is a Java library that allows developers to easily create their own spaced repetition system using the <a href="https://github.com/open-spaced-repetition/free-spaced-repetition-scheduler">Free Spaced Repetition Scheduler algorithm</a>.**

## Table of Contents
- [Installation](#installation)
- [Quickstart](#quickstart)

## Installation
You can install the `fsrs` Java library from [Maven Central](https://central.sonatype.com/artifact/io.github.open-spaced-repetition/fsrs) using Maven:
```xml
<dependency>
    <groupId>io.github.open-spaced-repetition</groupId>
    <artifactId>fsrs</artifactId>
    <version>0.1.1</version>
</dependency>
```

## Quickstart

```java
import io.github.openspacedrepetition.Scheduler;
import io.github.openspacedrepetition.Card;
import io.github.openspacedrepetition.Rating;
import io.github.openspacedrepetition.CardAndReviewLog;
import io.github.openspacedrepetition.ReviewLog;

import java.time.Instant;
import java.time.Duration;

public class SRS {

    public static void main(String[] args) {

        Scheduler scheduler = Scheduler.defaultScheduler();

        // note: all new cards are 'due' immediately upon creation
        Card card = new Card();

        // Choose a rating and review the card with the scheduler
        /*
        * Rating.AGAIN (==1) forgot the card
        * Rating.HARD (==2) remembered the card with serious difficulty
        * Rating.GOOD (==3) remembered the card after a hesitation
        * Rating.EASY (==4) remembered the card easily
        */
        Rating rating = Rating.GOOD;

        CardAndReviewLog result = scheduler.reviewCard(card, rating);
        card = result.card();
        ReviewLog reviewLog = result.reviewLog();

        System.out.println(
                "Card rated " + reviewLog.getRating() + " at " + reviewLog.getReviewDateTime());
        // > Card rated GOOD at 2025-07-10T04:16:19.637219Z

        // when the card is due next for review
        Instant due = card.getDue();

        // how much time between now and when the card is due
        Duration timeDelta = Duration.between(Instant.now(), due);

        System.out.println("Card due on: " + due);
        System.out.println("Card due in " + timeDelta.toSeconds() + " seconds");
        // > Card due on: 2025-07-10T04:26:19.637219Z
        // > Card due in 599 seconds

    }

}
```
