package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Activity;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.ActivityWindow;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.GetAccountBalanceUseCase.GetAccountBalanceQuery;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.LoadAccountPort;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetAccountBalanceServiceTest {

    private static final AccountId ACCOUNT_A = new AccountId(1L);
    private static final AccountId ACCOUNT_B = new AccountId(2L);
    private static final LocalDateTime T1 = LocalDateTime.of(2026, 9, 7, 10, 0);
    private static final LocalDateTime T2 = LocalDateTime.of(2026, 9, 7, 11, 0);

    @Mock
    private LoadAccountPort loadAccountPort;

    @InjectMocks
    private GetAccountBalanceService service;

    private static Activity 입금활동(LocalDateTime timestamp, long amount) {
        return new Activity(ACCOUNT_A, ACCOUNT_B, ACCOUNT_A, timestamp, Money.of(amount));
    }

    private static Activity 출금활동(LocalDateTime timestamp, long amount) {
        return new Activity(ACCOUNT_A, ACCOUNT_A, ACCOUNT_B, timestamp, Money.of(amount));
    }

    private static Account 계좌(long baselineBalance, Activity... activities) {
        return Account.withId(ACCOUNT_A, Money.of(baselineBalance), new ActivityWindow(activities));
    }

    @Nested
    class 잔액_조회 {

        @Test
        void 포트가_반환한_계좌의_잔액을_반환한다() {
            given(loadAccountPort.loadAccount(eq(ACCOUNT_A), any())).willReturn(계좌(500L));

            Money balance = service.getAccountBalance(new GetAccountBalanceQuery(ACCOUNT_A));

            assertThat(balance).isEqualTo(Money.of(500L));
        }

        @Test
        void 계좌의_활동이_반영된_잔액을_반환한다() {
            given(loadAccountPort.loadAccount(eq(ACCOUNT_A), any()))
                    .willReturn(계좌(500L, 입금활동(T1, 300L), 출금활동(T2, 100L)));

            Money balance = service.getAccountBalance(new GetAccountBalanceQuery(ACCOUNT_A));

            assertThat(balance).isEqualTo(Money.of(700L));
        }

        @Test
        void 잔액이_음수여도_그대로_반환한다() {
            given(loadAccountPort.loadAccount(eq(ACCOUNT_A), any()))
                    .willReturn(계좌(100L, 출금활동(T1, 300L)));

            Money balance = service.getAccountBalance(new GetAccountBalanceQuery(ACCOUNT_A));

            assertThat(balance).isEqualTo(Money.of(-200L));
        }
    }
}
