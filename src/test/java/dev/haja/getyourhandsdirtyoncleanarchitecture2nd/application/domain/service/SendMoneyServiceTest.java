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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
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
    @DisplayName("임계값을 초과하는 금액은 송금되지 않고 ThresholdExceededException이 발생함")
    void givenMoneyExceedsThreshold_thenThrowsThresholdExceededException() {

        // given
        SendMoneyService serviceWithLowThreshold = new SendMoneyService(
                loadAccountPort,
                accountLock,
                updateAccountStatePort,
                moneyTransferProperties(Money.of(1_000L)));

        SendMoneyCommand command = new SendMoneyCommand(
                new AccountId(41L),
                new AccountId(42L),
                Money.of(1_001L));

        // when / then
        assertThatThrownBy(() -> serviceWithLowThreshold.sendMoney(command))
                .isInstanceOf(ThresholdExceededException.class);

        // 임계값 체크가 가장 먼저 일어나므로 어떤 포트도 건드리지 않는다
        then(loadAccountPort).shouldHaveNoInteractions();
        then(accountLock).shouldHaveNoInteractions();
        then(updateAccountStatePort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("두 계좌를 커맨드의 ID와 10일 전 baselineDate로 조회함")
    void loadsBothAccountsWithBaselineDate() {

        // given
        Account sourceAccount = givenSourceAccount();
        Account targetAccount = givenTargetAccount();

        givenWithdrawalWillSucceed(sourceAccount);
        givenDepositWillSucceed(targetAccount);

        AccountId sourceAccountId = sourceAccount.getId().get();
        AccountId targetAccountId = targetAccount.getId().get();

        SendMoneyCommand command = new SendMoneyCommand(
                sourceAccountId,
                targetAccountId,
                Money.of(500L));

        LocalDateTime beforeSend = LocalDateTime.now();

        // when
        service.sendMoney(command);

        // then
        LocalDateTime afterSend = LocalDateTime.now();

        ArgumentCaptor<AccountId> accountIdCaptor = ArgumentCaptor.forClass(AccountId.class);
        ArgumentCaptor<LocalDateTime> baselineDateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        then(loadAccountPort).should(times(2))
                .loadAccount(accountIdCaptor.capture(), baselineDateCaptor.capture());

        // 출금 계좌를 먼저, 입금 계좌를 그다음에 조회한다
        assertThat(accountIdCaptor.getAllValues())
                .containsExactly(sourceAccountId, targetAccountId);

        // 서비스가 시각을 직접 읽으므로 정확한 값 대신 호출 전후로 만든 범위를 쓴다
        assertThat(baselineDateCaptor.getAllValues())
                .allSatisfy(baselineDate -> assertThat(baselineDate)
                        .isBetween(beforeSend.minusDays(10), afterSend.minusDays(10)));
    }

    @Test
    @DisplayName("출금 계좌에 ID가 없으면 IllegalStateException이 발생함")
    void givenSourceAccountHasNoId_thenThrowsIllegalStateException() {

        // given
        AccountId sourceAccountId = new AccountId(41L);
        AccountId targetAccountId = new AccountId(42L);

        givenAnAccountWithoutId(sourceAccountId);
        givenAnAccountWithId(targetAccountId);

        SendMoneyCommand command = new SendMoneyCommand(
                sourceAccountId,
                targetAccountId,
                Money.of(500L));

        // when / then
        assertThatThrownBy(() -> service.sendMoney(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("source account");

        // ID를 얻지 못하면 잠금도 저장도 일어나지 않는다
        then(accountLock).shouldHaveNoInteractions();
        then(updateAccountStatePort).shouldHaveNoInteractions();
    }

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
    @DisplayName("입금 실패 시 두 계좌 모두 잠금이 해제되고 계좌 상태는 저장되지 않음")
    void givenDepositFails_thenBothAccountsAreReleasedAndNothingIsPersisted() {

        // given
        Account sourceAccount = givenSourceAccount();
        Account targetAccount = givenTargetAccount();

        givenWithdrawalWillSucceed(sourceAccount);
        givenDepositWillFail(targetAccount);

        AccountId sourceAccountId = sourceAccount.getId().get();
        AccountId targetAccountId = targetAccount.getId().get();

        SendMoneyCommand command = new SendMoneyCommand(
                sourceAccountId,
                targetAccountId,
                Money.of(300L));

        // when
        boolean sendMoneyResult = service.sendMoney(command);

        // then
        assertThat(sendMoneyResult).isFalse();

        then(accountLock).should().lockAccount(eq(sourceAccountId));
        then(accountLock).should().lockAccount(eq(targetAccountId));
        then(accountLock).should().releaseAccount(eq(sourceAccountId));
        then(accountLock).should().releaseAccount(eq(targetAccountId));

        then(updateAccountStatePort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("출금이 예외를 던져도 출금 계좌 잠금이 해제됨")
    void givenWithdrawalThrows_thenSourceAccountIsReleased() {

        // given
        Account sourceAccount = givenSourceAccount();
        Account targetAccount = givenTargetAccount();

        given(sourceAccount.withdraw(any(Money.class), any(AccountId.class)))
                .willThrow(new RuntimeException("boom"));

        AccountId sourceAccountId = sourceAccount.getId().get();
        AccountId targetAccountId = targetAccount.getId().get();

        SendMoneyCommand command = new SendMoneyCommand(
                sourceAccountId,
                targetAccountId,
                Money.of(300L));

        // when / then
        assertThatThrownBy(() -> service.sendMoney(command))
                .isInstanceOf(RuntimeException.class);

        // 출금 계좌 잠금만 잡힌 뒤 터졌으므로 그 잠금만 풀려야 한다
        then(accountLock).should().lockAccount(eq(sourceAccountId));
        then(accountLock).should().releaseAccount(eq(sourceAccountId));

        // 입금 계좌는 애초에 잠기지 않았으므로 풀 것도 없다
        then(accountLock).should(times(0)).lockAccount(eq(targetAccountId));
        then(accountLock).should(times(0)).releaseAccount(eq(targetAccountId));

        then(updateAccountStatePort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("계좌 상태 저장이 예외를 던져도 두 계좌 모두 잠금이 해제됨")
    void givenUpdateActivitiesThrows_thenBothAccountsAreReleased() {

        // given
        Account sourceAccount = givenSourceAccount();
        Account targetAccount = givenTargetAccount();

        givenWithdrawalWillSucceed(sourceAccount);
        givenDepositWillSucceed(targetAccount);

        AccountId sourceAccountId = sourceAccount.getId().get();
        AccountId targetAccountId = targetAccount.getId().get();

        willThrow(new RuntimeException("boom"))
                .given(updateAccountStatePort).updateActivities(any(Account.class));

        SendMoneyCommand command = new SendMoneyCommand(
                sourceAccountId,
                targetAccountId,
                Money.of(300L));

        // when / then
        assertThatThrownBy(() -> service.sendMoney(command))
                .isInstanceOf(RuntimeException.class);

        // 저장이 실패해도 잡아 둔 잠금은 모두 풀려야 한다
        then(accountLock).should().releaseAccount(eq(sourceAccountId));
        then(accountLock).should().releaseAccount(eq(targetAccountId));
    }

    @Test
    @DisplayName("입금이 예외를 던져도 두 계좌 모두 잠금이 해제됨")
    void givenDepositThrows_thenBothAccountsAreReleased() {

        // given
        Account sourceAccount = givenSourceAccount();
        Account targetAccount = givenTargetAccount();

        givenWithdrawalWillSucceed(sourceAccount);
        given(targetAccount.deposit(any(Money.class), any(AccountId.class)))
                .willThrow(new RuntimeException("boom"));

        AccountId sourceAccountId = sourceAccount.getId().get();
        AccountId targetAccountId = targetAccount.getId().get();

        SendMoneyCommand command = new SendMoneyCommand(
                sourceAccountId,
                targetAccountId,
                Money.of(300L));

        // when / then
        assertThatThrownBy(() -> service.sendMoney(command))
                .isInstanceOf(RuntimeException.class);

        // 입금 계좌 잠금까지 잡힌 뒤 터졌으므로 두 잠금 모두 풀려야 한다
        then(accountLock).should().releaseAccount(eq(sourceAccountId));
        then(accountLock).should().releaseAccount(eq(targetAccountId));

        then(updateAccountStatePort).shouldHaveNoInteractions();
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

    // 입금 계좌의 입금이 실패할 것이다.
    private void givenDepositWillFail(Account account) {
        given(account.deposit(any(Money.class), any(AccountId.class)))
                .willReturn(false);
    }


    private @NonNull Account givenAnAccountWithId(AccountId id) {
        Account account = Mockito.mock(Account.class);
        given(account.getId())
                .willReturn(Optional.of(id));
        given(loadAccountPort.loadAccount(eq(account.getId().get()), any(LocalDateTime.class)))
                .willReturn(account);
        return account;
    }

    // 주어진 ID로 조회되지만 정작 자신은 ID를 갖고 있지 않은 계좌
    private @NonNull Account givenAnAccountWithoutId(AccountId loadedForId) {
        Account account = Mockito.mock(Account.class);
        given(account.getId())
                .willReturn(Optional.empty());
        given(loadAccountPort.loadAccount(eq(loadedForId), any(LocalDateTime.class)))
                .willReturn(account);
        return account;
    }

    private MoneyTransferProperties moneyTransferProperties() {
        return moneyTransferProperties(Money.of(Long.MAX_VALUE));
    }

    private MoneyTransferProperties moneyTransferProperties(Money maximumTransferThreshold) {
        return new MoneyTransferProperties(maximumTransferThreshold);
    }
}
