package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.GetAccountBalanceUseCase.GetAccountBalanceQuery;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.NoSuchAccountException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.AccountNotFoundException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.LoadAccountPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountTestData.DEFAULT_ACCOUNT_ID;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.TimeTestData.CLOCK;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.TimeTestData.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

// 네이티브 이미지는 런타임 바이트코드 생성을 지원하지 않아 Mockito가 초기화되지 않는다.
// 이 유스케이스를 쓰는 인바운드 어댑터가 아직 없어 네이티브에서 대신 덮는 테스트는 없다 —
// 어댑터가 생기면 시스템 테스트가 덮는다.
@DisabledInNativeImage
class GetAccountBalanceServiceTest {

    private final LoadAccountPort loadAccountPort =
            Mockito.mock(LoadAccountPort.class);

    private final GetAccountBalanceService service =
            new GetAccountBalanceService(loadAccountPort, CLOCK);

    @Test
    @DisplayName("현재 시각을 baselineDate로 계좌를 조회해 그 잔액을 반환함")
    void returnsBalanceOfAccountLoadedWithCurrentTimeAsBaselineDate() {

        // given
        Account account = Mockito.mock(Account.class);
        given(account.calculateBalance())
                .willReturn(Money.of(500L));
        given(loadAccountPort.loadAccount(eq(DEFAULT_ACCOUNT_ID), any(LocalDateTime.class)))
                .willReturn(account);

        // when
        Money balance = service.getAccountBalance(new GetAccountBalanceQuery(DEFAULT_ACCOUNT_ID));

        // then
        assertThat(balance).isEqualTo(Money.of(500L));

        ArgumentCaptor<AccountId> accountIdCaptor = ArgumentCaptor.forClass(AccountId.class);
        ArgumentCaptor<LocalDateTime> baselineDateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        then(loadAccountPort).should()
                .loadAccount(accountIdCaptor.capture(), baselineDateCaptor.capture());

        assertThat(accountIdCaptor.getValue()).isEqualTo(DEFAULT_ACCOUNT_ID);
        // 시각은 주입된 Clock에서 온다
        assertThat(baselineDateCaptor.getValue()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("계좌가 없으면 NoSuchAccountException이 발생함")
    void givenAccountDoesNotExist_thenThrowsNoSuchAccountException() {

        // given
        given(loadAccountPort.loadAccount(eq(DEFAULT_ACCOUNT_ID), any(LocalDateTime.class)))
                .willThrow(new AccountNotFoundException(DEFAULT_ACCOUNT_ID));

        // when / then
        // 아웃바운드 포트의 예외가 아니라 유스케이스의 예외가 올라온다
        assertThatThrownBy(() -> service.getAccountBalance(new GetAccountBalanceQuery(DEFAULT_ACCOUNT_ID)))
                .isInstanceOf(NoSuchAccountException.class)
                .hasMessageContaining("42")
                .hasCauseInstanceOf(AccountNotFoundException.class);
    }
}
