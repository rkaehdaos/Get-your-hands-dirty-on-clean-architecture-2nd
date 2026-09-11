package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyCommand;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class SendMoneyServiceTest {

    SendMoneyService service = new SendMoneyService(
            new MoneyTransferProperties(Money.of(1_000L)));

    @Test
    @DisplayName("한도 초과 시 송금 실패")
    void sendMoneyFailsWhenThresholdExceeded() {

        // given
        SendMoneyCommand command = new SendMoneyCommand(
                new AccountId(41L),
                new AccountId(42L),
                Money.of(1_001L));

        // when / then
        assertThatThrownBy(() -> service.sendMoney(command))
                .isInstanceOf(ThresholdExceededException.class);
    }

    @Test
    @DisplayName("금액이 임계값과 일치할 때 송금 성공")
    void sendMoneySucceedsWhenAmountEqualsThreshold() {

        // given
        SendMoneyCommand command = new SendMoneyCommand(
                new AccountId(41L),
                new AccountId(42L),
                Money.of(1_000L));

        // when / then
        assertThatCode(() -> service.sendMoney(command))
                .doesNotThrowAnyException();
    }
}
