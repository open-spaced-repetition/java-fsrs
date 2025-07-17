/* (C)2025 */
package io.github.openspacedrepetition;

import lombok.Getter;

@Getter
public enum Rating {
    AGAIN(1),
    HARD(2),
    GOOD(3),
    EASY(4);

    private final int value;

    Rating(int value) {
        this.value = value;
    }
}
