package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.LoadAccountPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class GetAccountBalanceService {
    private final LoadAccountPort loadAccountPort;

}
