package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.ActivityTestData.defaultActivity;

import static org.assertj.core.api.Assertions.*;

class ActivityWindowTest {

    @Test
    void calculatesStartTimestamp() {
        ActivityWindow window = new ActivityWindow(
                defaultActivity().withTimestamp(startDate()).build(),
                defaultActivity().withTimestamp(inBetweenDate()).build(),
                defaultActivity().withTimestamp(endDate()).build()
        );
        assertThat(window.getStartTimestamp()).isEqualTo(startDate());
    }

    private static @NonNull LocalDateTime endDate() {
        return LocalDateTime.of(2026, 9, 5, 0, 0);
    }

    private static @NonNull LocalDateTime inBetweenDate() {
        return LocalDateTime.of(2026, 9, 4, 0, 0);
    }

    private static @NonNull LocalDateTime startDate() {
        return LocalDateTime.of(2026, 9, 3, 0, 0);
    }
}
