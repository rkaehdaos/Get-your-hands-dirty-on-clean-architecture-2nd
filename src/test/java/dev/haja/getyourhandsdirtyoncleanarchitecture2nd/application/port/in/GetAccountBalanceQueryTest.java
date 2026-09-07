package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.GetAccountBalanceUseCase.GetAccountBalanceQuery;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetAccountBalanceQueryTest {

    private static final AccountId ACCOUNT = new AccountId(1L);

    @Nested
    class 생성 {

        @Test
        void 전달한_값이_컴포넌트에_그대로_담긴다() {
            GetAccountBalanceQuery query = new GetAccountBalanceQuery(ACCOUNT);

            assertThat(query.accountId()).isEqualTo(ACCOUNT);
        }

        @Test
        void 유효한_값이면_검증을_통과한다() {
            assertThat(new GetAccountBalanceQuery(ACCOUNT)).isNotNull();
        }
    }

    @Nested
    class 자기_검증 {

        @Test
        void accountId가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new GetAccountBalanceQuery(null))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("accountId");
        }
    }

    @Nested
    class 동등성 {

        @Test
        void 같은_accountId면_동등하다() {
            GetAccountBalanceQuery a = new GetAccountBalanceQuery(ACCOUNT);
            GetAccountBalanceQuery b = new GetAccountBalanceQuery(new AccountId(1L));

            assertThat(a).isEqualTo(b);
            assertThat(a).hasSameHashCodeAs(b);
        }

        @Test
        void accountId가_다르면_동등하지_않다() {
            GetAccountBalanceQuery a = new GetAccountBalanceQuery(ACCOUNT);
            GetAccountBalanceQuery b = new GetAccountBalanceQuery(new AccountId(2L));

            assertThat(a).isNotEqualTo(b);
        }
    }
}
