package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.adapter.in.web;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in.SendMoneyUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class SendMoneyController {
    private final SendMoneyUseCase sendMoneyUseCase;
}
