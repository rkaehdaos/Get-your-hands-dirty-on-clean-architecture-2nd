package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.in.web;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.InsufficientFundsException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.NoSuchAccountException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.ThresholdExceededException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class SendMoneyExceptionHandler {

    @ExceptionHandler({ThresholdExceededException.class, InsufficientFundsException.class})
    ProblemDetail handleTransferRejected(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }

    /**
     * 계좌가 없는 것은 이체를 거부당한 것이 아니라 요청한 자원이 없는 것이므로 404다.
     */
    @ExceptionHandler(NoSuchAccountException.class)
    ProblemDetail handleNoSuchAccount(NoSuchAccountException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /**
     * 커맨드의 자기 검증 실패는 이체 거부와 성격이 다르다 — 요청 자체가 잘못됐으므로 400이다.
     * 컨트롤러가 커맨드를 만들다 던지는 예외라 인자 바인딩 단계가 아닌 여기에 닿는다.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleInvalidCommand(ConstraintViolationException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
