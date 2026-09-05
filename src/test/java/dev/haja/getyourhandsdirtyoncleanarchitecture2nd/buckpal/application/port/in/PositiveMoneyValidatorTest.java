package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PositiveMoneyValidatorTest {

    private final PositiveMoneyValidator validator = new PositiveMoneyValidator();

    @Test
    void null은_유효하다_NotNull에_위임한다() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void 양수는_유효하다() {
        assertThat(validator.isValid(Money.of(1L), null)).isTrue();
    }

    @Test
    void 영은_유효하지_않다() {
        assertThat(validator.isValid(Money.ZERO, null)).isFalse();
    }

    @Test
    void 음수는_유효하지_않다() {
        assertThat(validator.isValid(Money.of(-1L), null)).isFalse();
    }
}
