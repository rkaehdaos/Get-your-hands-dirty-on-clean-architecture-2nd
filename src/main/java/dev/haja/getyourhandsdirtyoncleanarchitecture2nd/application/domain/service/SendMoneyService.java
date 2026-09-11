package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyCommand;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyUseCase;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.AccountLock;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.LoadAccountPort;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.UpdateAccountStatePort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Transactional
class SendMoneyService implements SendMoneyUseCase {

    private final LoadAccountPort loadAccountPort;
    private final AccountLock accountLock;
    private final UpdateAccountStatePort updateAccountStatePort;
    private final MoneyTransferProperties moneyTransferProperties;

    @Override
    public boolean sendMoney(SendMoneyCommand command) {

        // TODO: 비즈니스 규칙 검증
        checkThreshold(command);

        // TODO: model 상태 변경
        LocalDateTime baselineDate = LocalDateTime.now().minusDays(10);

        Account sourceAccount = loadAccountPort.loadAccount(
                command.sourceAccountId(),
                baselineDate);

        Account targetAccount = loadAccountPort.loadAccount(
                command.targetAccountId(),
                baselineDate);

        AccountId sourceAccountId = sourceAccount.getId()
                .orElseThrow(() -> new IllegalStateException("expected source account ID not to be empty"));
        AccountId targetAccountId = targetAccount.getId()
                .orElseThrow(() -> new IllegalStateException("expected target account ID not to be empty"));

        accountLock.lockAccount(sourceAccountId);
        if (!sourceAccount.withdraw(command.money(), targetAccountId)) {
            accountLock.releaseAccount(sourceAccountId);
            return false;
        }

        accountLock.lockAccount(targetAccountId);
        if (!targetAccount.deposit(command.money(), sourceAccountId)) {
            accountLock.releaseAccount(sourceAccountId);
            accountLock.releaseAccount(targetAccountId);
            return false;
        }

        updateAccountStatePort.updateActivities(sourceAccount);
        updateAccountStatePort.updateActivities(targetAccount);

        accountLock.releaseAccount(sourceAccountId);
        accountLock.releaseAccount(targetAccountId);

        // TODO: return
        return true;
    }

    private void checkThreshold(SendMoneyCommand command) {
        if(command.money().isGreaterThan(moneyTransferProperties.maximumTransferThreshold())){
            throw new ThresholdExceededException(moneyTransferProperties.maximumTransferThreshold(), command.money());
        }
    }
}
