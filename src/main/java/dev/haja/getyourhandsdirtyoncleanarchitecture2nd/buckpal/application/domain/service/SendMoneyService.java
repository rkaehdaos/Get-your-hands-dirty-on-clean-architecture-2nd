package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in.SendMoneyCommand;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in.SendMoneyUseCase;
import jakarta.validation.constraints.NotNull;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Account.AccountId;

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
    private void requireAccountExists(@NotNull AccountId accountId) {
        //TODO: 검사에 실패할 경우 전용 예외를 발생시키는 메서드를 호출.
        // 사용자와 인터페이스하는 어댑터는 이 예외를 오류 메시지로 사용자에게 표시하거나,
        // 적절하다고 판단되는 다른 방식으로 처리할 수 있습니다.
    }
}
