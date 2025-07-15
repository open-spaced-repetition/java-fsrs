/* (C)2025 */
package io.github.openspacedrepetition;

import java.time.Instant;

public record ReviewLog(
        int cardId, Rating rating, Instant reviewDatetime, Integer reviewDuration) {}
