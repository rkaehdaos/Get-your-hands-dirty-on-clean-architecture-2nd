package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in.SendMoneyCommand;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in.SendMoneyUseCase;

public class SendMoneyService implements SendMoneyUseCase {
    @Override
    public boolean sendMoney(SendMoneyCommand command) {
        // TODO: validate business rules
        // TODO: manipulate model state
        // TODO: return output
        return false;
    }
}
