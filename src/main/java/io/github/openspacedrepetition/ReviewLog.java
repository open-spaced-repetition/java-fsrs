/* (C)2025 */
package io.github.openspacedrepetition;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NonNull;
import java.time.Instant;

public record ReviewLog(
        @JsonProperty("cardId") int cardId,
        @NonNull @JsonProperty("rating") Rating rating,
        @NonNull @JsonProperty("review_datetime") @JsonFormat(shape = JsonFormat.Shape.STRING)
                Instant reviewDatetime,
        @JsonProperty("reviewDuration") Integer reviewDuration) {

    public ReviewLog(@NonNull ReviewLog otherReviewLog) {
        this(
                otherReviewLog.cardId,
                otherReviewLog.rating,
                otherReviewLog.reviewDatetime,
                otherReviewLog.reviewDuration);
    }

    public String toJson() {

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }

    public static ReviewLog fromJson(@NonNull String json) {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try {
            return mapper.readValue(json, ReviewLog.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
