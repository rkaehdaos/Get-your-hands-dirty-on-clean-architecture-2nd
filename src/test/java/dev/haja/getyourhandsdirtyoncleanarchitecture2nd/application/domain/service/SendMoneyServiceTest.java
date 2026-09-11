package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyCommand;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.AccountLock;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.LoadAccountPort;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.UpdateAccountStatePort;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;


class SendMoneyServiceTest {

    private final LoadAccountPort loadAccountPort =
            Mockito.mock(LoadAccountPort.class);

    private final AccountLock accountLock =
            Mockito.mock(AccountLock.class);

    private final UpdateAccountStatePort updateAccountStatePort =
            Mockito.mock(UpdateAccountStatePort.class);


    SendMoneyService service = new SendMoneyService(
            loadAccountPort,
            accountLock,
            updateAccountStatePort,
            moneyTransferProperties());

    @Test
    void account_mocking_test() {

        // given
        Long testLongValue = 999L;
        AccountId id = new AccountId(testLongValue);

        // when
        Account account = givenAnAccountWithId(id);

        // then
        assertThat(account.getId().get().value()).isEqualTo(testLongValue);
    }

    @Test
    @DisplayName("거래 성공")
    void transactionSucceeds() {

        // given
        AccountId sourceAccountId = new AccountId(41L);
        Account sourceAccount = givenAnAccountWithId(sourceAccountId);

        AccountId targetAccountId = new AccountId(42L);
        Account targetAccount = givenAnAccountWithId(targetAccountId);

        // 출금 계좌의 출금이 성공 mocking
        given(sourceAccount.withdraw(any(Money.class), any(AccountId.class)))
                .willReturn(true);
        // 입금 계좌의 입금이 성공 mocking
        given(targetAccount.deposit(any(Money.class), any(AccountId.class)))
                .willReturn(true);

        Money money = Money.of(500L);

        SendMoneyCommand command = new SendMoneyCommand(
                sourceAccount.getId().get(),
                targetAccount.getId().get(),
                money);

        // when
        boolean sendMoneyResult = service.sendMoney(command);

        // then
        assertThat(sendMoneyResult).isTrue();
    }


    @Test
    @DisplayName("한도 초과 시 송금 실패")
    void sendMoneyFailsWhenThresholdExceeded() {

        // given
        SendMoneyCommand command = new SendMoneyCommand(
                new AccountId(41L),
                new AccountId(42L),
                Money.of(1_001L));

        // when / then
        assertThatThrownBy(() -> service.sendMoney(command))
                .isInstanceOf(ThresholdExceededException.class);
    }

    @Test
    @DisplayName("금액이 임계값과 일치할 때 송금 성공")
    void sendMoneySucceedsWhenAmountEqualsThreshold() {

        // given
        AccountId sourceAccountId = new AccountId(41L);
        Account sourceAccount = givenAnAccountWithId(sourceAccountId);
        AccountId targetAccountId = new AccountId(42L);
        Account targetAccount = givenAnAccountWithId(targetAccountId);
        given(sourceAccount.withdraw(any(Money.class), any(AccountId.class))).willReturn(true);
        given(targetAccount.deposit(any(Money.class), any(AccountId.class))).willReturn(true);

        SendMoneyCommand command = new SendMoneyCommand(sourceAccountId, targetAccountId, Money.of(1_000L));


        // when / then
        assertThatCode(() -> service.sendMoney(command))
                .doesNotThrowAnyException();
    }

    private @NonNull Account givenAnAccountWithId(AccountId id) {
        Account account = Mockito.mock(Account.class);
        given(account.getId())
                .willReturn(Optional.of(id));
        given(loadAccountPort.loadAccount(eq(account.getId().get()), any(LocalDateTime.class)))
                .willReturn(account);
        return account;
    }

    private MoneyTransferProperties moneyTransferProperties() {
        return new MoneyTransferProperties(Money.of(1_000L));
    }
}
