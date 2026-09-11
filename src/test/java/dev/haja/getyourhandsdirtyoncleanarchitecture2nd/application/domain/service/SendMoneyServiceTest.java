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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

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
    @DisplayName("인출 실패 시 오직 출금 계좌만 잠겼다가 잠금이 해제됨")
    void givenWithdrawalFails_thenOnlySourceAccountIsLockedAndReleased() {

        // given
        AccountId sourceAccountId = new AccountId(41L);
        Account sourceAccount = givenAnAccountWithId(sourceAccountId);
        AccountId targetAccountId = new AccountId(42L);
        Account targetAccount = givenAnAccountWithId(targetAccountId);

        givenWithdrawalWillFail(sourceAccount);
        givenDepositWillSucceed(targetAccount);

        SendMoneyCommand command = new SendMoneyCommand(
                sourceAccountId,
                targetAccountId,
                Money.of(300L));

        // when
        boolean sendMoneyResult = service.sendMoney(command);

        // then
        assertThat(sendMoneyResult).isFalse();

        then(accountLock).should().lockAccount(eq(sourceAccountId));
        then(accountLock).should().releaseAccount(eq(sourceAccountId));
        then(accountLock).should(times(0)).lockAccount(eq(targetAccountId));

    }


    @Test
    @DisplayName("거래 성공")
    void transactionSucceeds() {

        // given
        Account sourceAccount = givenSourceAccount();
        Account targetAccount = givenTargetAccount();

        givenWithdrawalWillSucceed(sourceAccount);
        givenDepositWillSucceed(targetAccount);

        Money money = Money.of(500L);

        SendMoneyCommand command = new SendMoneyCommand(
                sourceAccount.getId().get(),
                targetAccount.getId().get(),
                money);

        // when
        boolean sendMoneyResult = service.sendMoney(command);

        // then
        assertThat(sendMoneyResult).isTrue();

        AccountId sourceAccountId = sourceAccount.getId().get();
        AccountId targetAccountId = targetAccount.getId().get();

        then(accountLock).should().lockAccount(eq(sourceAccountId));
        then(sourceAccount).should().withdraw(eq(money), eq(targetAccountId));
        then(accountLock).should().releaseAccount(eq(sourceAccountId));

        then(accountLock).should().lockAccount(eq(targetAccountId));
        then(targetAccount).should().deposit(eq(money), eq(sourceAccountId));
        then(accountLock).should().releaseAccount(eq(targetAccountId));

        // then Account들이 업데이트 된다.
        thenAccountsHaveBeenUpdated(sourceAccountId, targetAccountId);
    }

    private void thenAccountsHaveBeenUpdated(AccountId... accountIds) {
        // 출금 포트가 정확히 N번 호출됨
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        then(updateAccountStatePort).should(times(accountIds.length))
                .updateActivities(accountCaptor.capture());

        // 넘어간 계좌들이 기대한 ID를 포함함
        List<AccountId> updatedAccountIds = accountCaptor.getAllValues()
                .stream()
                .map(Account::getId)
                .map(Optional::get)
                .collect(Collectors.toList());

        assertThat(updatedAccountIds).containsExactlyInAnyOrder(accountIds);
    }


    private Account givenSourceAccount() {
        return givenAnAccountWithId(new AccountId(41L));
    }

    private Account givenTargetAccount() {
        return givenAnAccountWithId(new AccountId(42L));
    }

    // 출금 계좌의 출금이 실패할 것이다
    private void givenWithdrawalWillFail(Account account) {
        given(account.withdraw(any(Money.class), any(AccountId.class)))
                .willReturn(false);
    }

    // 출금 계좌의 출금이 성공할 것이다
    private void givenWithdrawalWillSucceed(Account account) {
        given(account.withdraw(any(Money.class), any(AccountId.class)))
                .willReturn(true);
    }

    // 입금 계좌의 입금이 성공할 것이다.
    private void givenDepositWillSucceed(Account account) {
        given(account.deposit(any(Money.class), any(AccountId.class)))
                .willReturn(true);
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
        return new MoneyTransferProperties(Money.of(Long.MAX_VALUE));
    }
}
