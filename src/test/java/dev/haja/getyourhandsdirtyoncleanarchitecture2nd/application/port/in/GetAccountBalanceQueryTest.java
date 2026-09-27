package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.GetAccountBalanceUseCase.GetAccountBalanceQuery;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountTestData.DEFAULT_ACCOUNT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetAccountBalanceQueryTest {

    @Test
    @DisplayName("계좌 ID가 null이면 쿼리 생성이 거부됨")
    void givenNullAccountId_thenThrowsConstraintViolationException() {

        // when / then
        assertThatThrownBy(() -> new GetAccountBalanceQuery(null))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("accountId");
    }

    @Test
    void createsQuery() {

        // when
        GetAccountBalanceQuery query = new GetAccountBalanceQuery(DEFAULT_ACCOUNT_ID);

        // then
        assertThat(query.accountId()).isEqualTo(DEFAULT_ACCOUNT_ID);
    }
}
