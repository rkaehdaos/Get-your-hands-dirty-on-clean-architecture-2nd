package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.out.LoadAccountPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetAccountBalanceService {
    private final LoadAccountPort loadAccountPort;
}
