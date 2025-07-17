/* (C)2025 */
package io.github.openspacedrepetition;

import lombok.Getter;

@Getter
public enum State {
    LEARNING(1),
    REVIEW(2),
    RELEARNING(3);

    private final int value;

    State(int value) {
        this.value = value;
    }
}
