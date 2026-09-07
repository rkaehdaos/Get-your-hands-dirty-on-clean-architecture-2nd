package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SendMoneyCommandTest {

    private static final AccountId SOURCE = new AccountId(1L);
    private static final AccountId TARGET = new AccountId(2L);
    private static final Money MONEY = Money.of(1000L);

    @Nested
    class 생성 {

        @Test
        void 모든_값을_전달하면_각_컴포넌트에_그대로_담긴다() {
            SendMoneyCommand command = new SendMoneyCommand(SOURCE, TARGET, MONEY);

            assertThat(command.sourceAccountId()).isEqualTo(SOURCE);
            assertThat(command.targetAccountId()).isEqualTo(TARGET);
            assertThat(command.money()).isEqualTo(MONEY);
        }

        @Test
        void 유효한_값이면_검증을_통과한다() {
            assertThat(new SendMoneyCommand(SOURCE, TARGET, MONEY)).isNotNull();
        }
    }

    @Nested
    class 자기_검증 {

        @Test
        void sourceAccountId가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new SendMoneyCommand(null, TARGET, MONEY))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("sourceAccountId");
        }

        @Test
        void targetAccountId가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new SendMoneyCommand(SOURCE, null, MONEY))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("targetAccountId");
        }

        @Test
        void money가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new SendMoneyCommand(SOURCE, TARGET, null))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("money");
        }
    }

    @Nested
    class 동등성 {

        @Test
        void 모든_컴포넌트가_같으면_동등하다() {
            SendMoneyCommand a = new SendMoneyCommand(SOURCE, TARGET, MONEY);
            SendMoneyCommand b = new SendMoneyCommand(SOURCE, TARGET, MONEY);

            assertThat(a).isEqualTo(b);
            assertThat(a).hasSameHashCodeAs(b);
        }

        @Test
        void money가_다르면_동등하지_않다() {
            SendMoneyCommand a = new SendMoneyCommand(SOURCE, TARGET, MONEY);
            SendMoneyCommand b = new SendMoneyCommand(SOURCE, TARGET, Money.of(2000L));

            assertThat(a).isNotEqualTo(b);
        }
    }
}
