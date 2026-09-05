package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.adapter.out.persistence;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Account.AccountId;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.out.LoadAccountPort;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
class AccountPersistenceAdapter implements
        LoadAccountPort {
    private final AccountRepository accountRepository;
    private final ActivityRepository activityRepository;
    private final AccountMapper accountMapper;

    @Override
    public Account loadAccount(
            AccountId accountId,
            LocalDateTime baselineDate) {
        AccountJpaEntity account =
                accountRepository.findById(accountId.value())
                        .orElseThrow(EntityNotFoundException::new);
        List<ActivityJpaEntity> activities =
                activityRepository.findByOwnerSince(
                        accountId.value(),
                        baselineDate);
        Long withdrawalBalance = activityRepository
                .getWithdrawalBalanceUntil(
                        accountId.value(),
                        baselineDate)
                .orElse(0L);
        Long depositBalance = activityRepository
                .getDepositBalanceUntil(
                        accountId.value(),
                        baselineDate)
                .orElse(0L);


        return accountMapper.mapToDomainEntity(
                account,
                activities,
                withdrawalBalance,
                depositBalance);
    }
}
