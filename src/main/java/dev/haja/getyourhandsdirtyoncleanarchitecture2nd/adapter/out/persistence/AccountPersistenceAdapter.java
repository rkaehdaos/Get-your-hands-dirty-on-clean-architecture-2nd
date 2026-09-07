package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AccountPersistenceAdapter {

    private final AccountMapper accountMapper;

}
