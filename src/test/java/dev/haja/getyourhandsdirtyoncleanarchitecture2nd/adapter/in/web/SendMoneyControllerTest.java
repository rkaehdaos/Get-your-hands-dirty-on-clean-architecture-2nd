package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.in.web;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.InsufficientFundsException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyCommand;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyUseCase;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.ThresholdExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = SendMoneyController.class)
class SendMoneyControllerTest {

    @Autowired private MockMvcTester mockMvcTester;
    @MockitoBean private SendMoneyUseCase sendMoneyUseCase;

    @Test
    void testSendMoney() {

        // when
        var result = mockMvcTester.post()
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}", 41L, 42L, 500)
                .header("Content-Type", "application/json")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.OK);

        then(sendMoneyUseCase).should()
                .sendMoney(eq(new SendMoneyCommand(
                        new AccountId(41L),
                        new AccountId(42L),
                        Money.of(500L))));
    }

    @Test
    @DisplayName("잔액 부족으로 이체가 거부되면 422 Unprocessable Entity가 응답됨")
    void givenInsufficientFunds_thenRespondsWithUnprocessableEntity() {

        // given
        willThrow(new InsufficientFundsException(new AccountId(41L), Money.of(500L)))
                .given(sendMoneyUseCase).sendMoney(any(SendMoneyCommand.class));

        // when
        var result = mockMvcTester.post()
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}", 41L, 42L, 500)
                .header("Content-Type", "application/json")
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .extractingPath("$.detail")
                .asString()
                .contains("41", "500");
    }

    @Test
    @DisplayName("한도를 초과한 이체가 거부되면 422 Unprocessable Entity가 응답됨")
    void givenThresholdExceeded_thenRespondsWithUnprocessableEntity() {

        // given
        willThrow(new ThresholdExceededException(Money.of(1_000_000L), Money.of(2_000_000L)))
                .given(sendMoneyUseCase).sendMoney(any(SendMoneyCommand.class));

        // when
        var result = mockMvcTester.post()
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}", 41L, 42L, 2_000_000)
                .header("Content-Type", "application/json")
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .extractingPath("$.detail")
                .asString()
                .contains("1000000", "2000000");
    }
}