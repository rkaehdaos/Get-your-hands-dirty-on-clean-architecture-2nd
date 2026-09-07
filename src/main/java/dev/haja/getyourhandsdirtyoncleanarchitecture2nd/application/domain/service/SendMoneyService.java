package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyCommand;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyUseCase;

class SendMoneyService implements SendMoneyUseCase {
    @Override
    public boolean sendMoney(SendMoneyCommand sendMoneyCommand) {
        // TODO: 비즈니스 규칙 검증
        // TODO: model 상태 변경
        // TODO: return output
        throw new UnsupportedOperationException("Not implemented");
    }
}
