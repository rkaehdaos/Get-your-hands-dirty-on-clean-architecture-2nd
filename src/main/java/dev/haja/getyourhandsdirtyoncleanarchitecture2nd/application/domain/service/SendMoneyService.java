package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.InsufficientFundsException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.NoSuchAccountException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyCommand;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyUseCase;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.ThresholdExceededException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.AccountLock;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.AccountNotFoundException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.LoadAccountPort;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.UpdateAccountStatePort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Transactional
class SendMoneyService implements SendMoneyUseCase {

    private final LoadAccountPort loadAccountPort;
    private final AccountLock accountLock;
    private final UpdateAccountStatePort updateAccountStatePort;
    private final MoneyTransferProperties moneyTransferProperties;

    /**
     * 현재 시각을 직접 가져오지 않고 Clock에게 물어본다. 시각이 주입 가능한 의존성이 되어
     * 테스트가 baselineDate를 정확히 고정할 수 있다.
     */
    private final Clock clock;

    @Override
    public void sendMoney(SendMoneyCommand command) {

        checkThreshold(command);

        LocalDateTime baselineDate = LocalDateTime.now(clock).minusDays(10);

        Account sourceAccount = loadAccount(command.sourceAccountId(), baselineDate);
        Account targetAccount = loadAccount(command.targetAccountId(), baselineDate);

        AccountId sourceAccountId = sourceAccount.getId()
                .orElseThrow(() -> new IllegalStateException("expected source account ID not to be empty"));
        AccountId targetAccountId = targetAccount.getId()
                .orElseThrow(() -> new IllegalStateException("expected target account ID not to be empty"));

        accountLock.lockAccount(sourceAccountId);
        try {
            if (!sourceAccount.withdraw(command.money(), targetAccountId)) {
                throw new InsufficientFundsException(sourceAccountId, command.money());
            }

            accountLock.lockAccount(targetAccountId);
            try {
                if (!targetAccount.deposit(command.money(), sourceAccountId)) {
                    throw new IllegalStateException("expected deposit to target account to succeed");
                }

                updateAccountStatePort.updateActivities(sourceAccount);
                updateAccountStatePort.updateActivities(targetAccount);
            } finally {
                accountLock.releaseAccount(targetAccountId);
            }
        } finally {
            accountLock.releaseAccount(sourceAccountId);
        }
    }

    /**
     * 포트가 던진 예외는 감싸지 않고 그대로 전파하는 것이 기본이지만, "계좌가 없다"는
     * 저장소의 사정이 아니라 유스케이스가 거부됐다는 사실이므로 {@code port.in}의 예외로
     * 번역한다. 인바운드 어댑터가 {@code port.out}을 알지 않아도 된다.
     */
    private Account loadAccount(AccountId accountId, LocalDateTime baselineDate) {
        try {
            return loadAccountPort.loadAccount(accountId, baselineDate);
        } catch (AccountNotFoundException e) {
            throw new NoSuchAccountException(accountId, e);
        }
    }

    private void checkThreshold(SendMoneyCommand command) {
        if(command.money().isGreaterThan(moneyTransferProperties.maximumTransferThreshold())){
            throw new ThresholdExceededException(moneyTransferProperties.maximumTransferThreshold(), command.money());
        }
    }
}
