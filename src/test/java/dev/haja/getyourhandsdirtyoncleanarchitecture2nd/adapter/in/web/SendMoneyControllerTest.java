package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.in.web;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.InsufficientFundsException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.NoSuchAccountException;
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
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountTestData.DEFAULT_ACCOUNT_ID;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountTestData.OTHER_ACCOUNT_ID;
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
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
                        OTHER_ACCOUNT_ID.value(), DEFAULT_ACCOUNT_ID.value(), 500)
                .header("Content-Type", "application/json")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.OK);

        then(sendMoneyUseCase).should()
                .sendMoney(eq(new SendMoneyCommand(
                        OTHER_ACCOUNT_ID,
                        DEFAULT_ACCOUNT_ID,
                        Money.of(500L))));
    }

    @Test
    @DisplayName("잔액 부족으로 이체가 거부되면 422 Unprocessable Entity가 응답됨")
    void givenInsufficientFunds_thenRespondsWithUnprocessableEntity() {

        // given
        willThrow(new InsufficientFundsException(OTHER_ACCOUNT_ID, Money.of(500L)))
                .given(sendMoneyUseCase).sendMoney(any(SendMoneyCommand.class));

        // when
        var result = mockMvcTester.post()
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
                        OTHER_ACCOUNT_ID.value(), DEFAULT_ACCOUNT_ID.value(), 500)
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
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
                        OTHER_ACCOUNT_ID.value(), DEFAULT_ACCOUNT_ID.value(), 2_000_000)
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

    @Test
    @DisplayName("계좌가 없으면 404 Not Found가 응답됨")
    void givenNoSuchAccount_thenRespondsWithNotFound() {

        // given
        willThrow(new NoSuchAccountException(new AccountId(999L), new RuntimeException("boom")))
                .given(sendMoneyUseCase).sendMoney(any(SendMoneyCommand.class));

        // when
        var result = mockMvcTester.post()
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
                        999L, DEFAULT_ACCOUNT_ID.value(), 500)
                .header("Content-Type", "application/json")
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .extractingPath("$.detail")
                .asString()
                .contains("999");
    }

    @Test
    @DisplayName("금액이 양수가 아니면 커맨드 검증에 실패해 400 Bad Request가 응답됨")
    void givenNonPositiveAmount_thenRespondsWithBadRequest() {

        // when
        // 유스케이스 목을 스터빙할 필요가 없다 — 컨트롤러가 커맨드를 만드는 자리에서 터진다
        var result = mockMvcTester.post()
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
                        OTHER_ACCOUNT_ID.value(), DEFAULT_ACCOUNT_ID.value(), 0)
                .header("Content-Type", "application/json")
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .extractingPath("$.detail")
                .asString()
                .contains("money");

        then(sendMoneyUseCase).shouldHaveNoInteractions();
    }
}