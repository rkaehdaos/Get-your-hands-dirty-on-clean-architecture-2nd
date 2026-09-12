package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

public interface SendMoneyUseCase {

    /**
     * @throws ThresholdExceededException 이체액이 허용 한도를 초과하면
     * @throws NoSuchAccountException     출금 또는 입금 계좌가 없으면
     * @throws InsufficientFundsException 출금 계좌의 잔액이 부족하면
     */
    void sendMoney(SendMoneyCommand sendMoneyCommand);
}
