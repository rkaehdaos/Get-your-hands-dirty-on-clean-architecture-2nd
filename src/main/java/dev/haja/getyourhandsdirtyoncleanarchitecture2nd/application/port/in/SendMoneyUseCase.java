package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

public interface SendMoneyUseCase {
    boolean sendMoney(SendMoneyCommand sendMoneyCommand);
}
