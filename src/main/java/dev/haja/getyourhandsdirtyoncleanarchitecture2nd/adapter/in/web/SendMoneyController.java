package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.in.web;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SendMoneyController {
    @PostMapping(path = "/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}")
    void sendMoney(@PathVariable("sourceAccountId") Long sourceAccountId,
                   @PathVariable("targetAccountId") Long targetAccountId,
                   @PathVariable("amount") Long amount) {}
}
