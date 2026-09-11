package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyCommand;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class SendMoneyServiceTest {

    @Test
    void sendMoneyThrowsUnsupportedOperationException() {

        // given
        SendMoneyService service = new SendMoneyService();
        AccountId sourceAccountId = new AccountId(41L);
        AccountId targetAccountId = new AccountId(42L);
        SendMoneyCommand command = new SendMoneyCommand(
                sourceAccountId,
                targetAccountId,
                Money.of(300L));


        // when
        boolean result = service.sendMoney(command);
        // then
        assertThat(result).isTrue();

    }
}