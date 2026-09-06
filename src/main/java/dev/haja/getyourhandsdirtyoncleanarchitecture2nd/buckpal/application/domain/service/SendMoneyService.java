package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in.SendMoneyCommand;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in.SendMoneyUseCase;
import jakarta.validation.constraints.NotNull;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Account.AccountId;
import org.springframework.stereotype.Component;

@Component
public class SendMoneyService implements SendMoneyUseCase {
    @Override
    public boolean sendMoney(SendMoneyCommand command) {
        // TODO: validate business rules
        requireAccountExists(command.sourceAccountId());
        requireAccountExists(command.targetAccountId());

        // TODO: manipulate model state
        // TODO: return output
        return false;
    }

    // 도메인 엔티티에서 비즈니스 규칙을 검증하는 것이 불가능한 경우,
    // 도메인 엔티티에 대한 처리가 시작되기 전에
    // 유스 케이스 코드에서 이를 수행할 수 있습니다:
    private void requireAccountExists(AccountId accountId) {
        if (accountId==null)
            throw new IllegalStateException("expected source account ID not to be empty");
    }
}
