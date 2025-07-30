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
    <a href="https://javadoc.io/doc/io.github.open-spaced-repetition/fsrs"><img src="https://javadoc.io/badge2/io.github.open-spaced-repetition/fsrs/javadoc.svg" alt="javadoc"></a>
    <a href="https://central.sonatype.com/artifact/io.github.open-spaced-repetition/fsrs"><img src="https://img.shields.io/badge/Java-17-blue.svg"></a>
    <a href="https://github.com/open-spaced-repetition/java-fsrs/blob/main/LICENSE" style="text-decoration: none;"><img src="https://img.shields.io/badge/License-MIT-brightgreen.svg"></a>
        <a href="https://codecov.io/gh/open-spaced-repetition/java-fsrs" ><img src="https://codecov.io/gh/open-spaced-repetition/java-fsrs/graph/badge.svg"/></a>
</div>
<br />

**Java-FSRS is a Java library that allows developers to easily create their own spaced repetition system using the <a href="https://github.com/open-spaced-repetition/free-spaced-repetition-scheduler">Free Spaced Repetition Scheduler algorithm</a>.**

## Table of Contents
- [Installation](#installation)
- [Quickstart](#quickstart)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Other FSRS implementations](#other-fsrs-implementations)
- [Contribute](#contribute)

## Installation
You can install the `fsrs` Java library from [Maven Central](https://central.sonatype.com/artifact/io.github.open-spaced-repetition/fsrs).

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

        Scheduler scheduler = Scheduler.builder().build();

        // note: all new cards are 'due' immediately upon creation
        Card card = Card.builder().build();

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
                "Card rated " + reviewLog.rating() + " at " + reviewLog.reviewDatetime());
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

## Usage

### Custom parameters

You can initialize the FSRS scheduler with your own custom parameters.

```java

// NOTE: the following arguments are also the defaults
Scheduler scheduler =
        Scheduler.builder()
                .parameters(
                        new double[] {
                                0.2172, 1.1771, 3.2602, 16.1507, 7.0114, 0.57, 2.0966, 0.0069,
                                1.5261, 0.112, 1.0178, 1.849, 0.1133, 0.3127, 2.2934, 0.2191,
                                3.0004, 0.7536, 0.3332, 0.1437, 0.2
                        })
                .desiredRetention(0.9)
                .learningSteps(
                        new Duration[] {Duration.ofMinutes(1), Duration.ofMinutes(10)})
                .relearningSteps(new Duration[] {Duration.ofMinutes(10)})
                .maximumInterval(36500)
                .enableFuzzing(true)
                .build();

```

#### Explanation of parameters

`parameters` are a set of 21 model weights that affect how the FSRS scheduler will schedule future reviews. If you're not familiar with optimizing FSRS, it is best not to modify these default values.

`desiredRetention` is a value between 0 and 1 that sets the desired minimum retention rate for cards when scheduled with the scheduler. For example, with the default value of `desiredRetention=0.9`, a card will be scheduled at a time in the future when the predicted probability of the user correctly recalling that card falls to 90%. A higher `desiredRetention` rate will lead to more reviews and a lower rate will lead to fewer reviews.

`learningSteps` are custom time intervals that schedule new cards in the Learning state. By default, cards in the Learning state have short intervals of 1 minute then 10 minutes. You can also disable `learningSteps` with `Scheduler.builder().learningSteps(new Duration[] {}).build();`

`relearningSteps` are analogous to `learningSteps` except they apply to cards in the Relearning state. Cards transition to the Relearning state if they were previously in the Review state, then were rated Again - this is also known as a 'lapse'. If you specify `Scheduler.builder().relearningSteps(new Duration[] {}).build();`, cards in the Review state, when lapsed, will not move to the Relearning state, but instead stay in the Review state.

`maximumInterval` sets the cap for the maximum days into the future the scheduler is capable of scheduling cards. For example, if you never want the scheduler to schedule a card more than one year into the future, you'd set `Scheduler.builder().maximumInterval(365).build();`.

`enableFuzzing`, if set to True, will apply a small amount of random 'fuzz' to calculated intervals. For example, a card that would've been due in 50 days, after fuzzing, might be due in 49, or 51 days.


### Timezone

**Java-FSRS uses UTC only.**

### Retrievability

You can calculate the current probability of correctly recalling a card (its 'retrievability') with

```java
double retrievability = scheduler.getCardRetrievability(card);

System.out.println("There is a " + retrievability + " probability that this card is remembered");
// > There is a 0.94 probability that this card is remembered
```

### Serialization

`Scheduler`, `Card` and `ReviewLog` objects are all JSON-serializable via their `toJson` and `fromJson` methods for easy database storage:

```java
// serialize before storage
String schedulerJson = scheduler.toJson();
String cardJson = card.toJson();
String reviewLogJson = reviewLog.toJson();

// deserialize from json string
Scheduler newScheduler = Scheduler.fromJson(schedulerJson);
Card newCard = Card.fromJson(cardJson);
ReviewLog newReviewLog = ReviewLog.fromJson(reviewLogJson);
```

### Optimizer

Currently, Java-FSRS does not support parameter optimization. If you'd like to optimize your parameters, please see either [fsrs-rs](https://github.com/open-spaced-repetition/fsrs-rs) or [py-fsrs](https://github.com/open-spaced-repetition/py-fsrs).

## API Documentation

You can find javadoc documentation for java-fsrs [here](https://javadoc.io/doc/io.github.open-spaced-repetition/fsrs).

## Other FSRS implementations

You can find various other FSRS implementations and projects [here](https://github.com/orgs/open-spaced-repetition/repositories?q=fsrs+sort%3Astars).

## Contribute

If you encounter issues with `java-fsrs` or would like to contribute code, please see [CONTRIBUTING](CONTRIBUTING.md).