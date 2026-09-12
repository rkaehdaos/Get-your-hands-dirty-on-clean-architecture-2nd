package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Activity;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.LoadAccountPort;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.UpdateAccountStatePort;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 계좌를 읽고 쓰는 영속성 어댑터.
 * <p>
 * {@code @Transactional}은 이 어댑터를 <b>자족적으로</b> 만들기 위한 것이다. 운영 경로에서는
 * {@code SendMoneyService}의 트랜잭션에 참여하지만(REQUIRED), 트랜잭션 없이 호출되더라도
 * {@code updateActivities}의 여러 INSERT가 전부 반영되거나 전부 취소되어야 한다.
 */
@Component
@RequiredArgsConstructor
@Transactional
class AccountPersistenceAdapter implements
        LoadAccountPort,
        UpdateAccountStatePort {

    private final SpringDataAccountRepository accountRepository;
    private final ActivityRepository activityRepository;
    private final AccountMapper accountMapper;

    @Override
    public Account loadAccount(AccountId accountId, LocalDateTime baselineDate) {
        AccountJpaEntity account =
                accountRepository.findById(accountId.value())
                        .orElseThrow(EntityNotFoundException::new);

        List<ActivityJpaEntity> activities =
                activityRepository.findByOwnerSince(
                        accountId.value(),
                        baselineDate);

        ActivityRepository.BaselineBalanceView baselineBalance =
                activityRepository.getBaselineBalanceUntil(
                        accountId.value(),
                        baselineDate);

        return accountMapper.mapToDomainEntity(
                account,
                activities,
                baselineBalance);
    }

    @Override
    public void updateActivities(Account account) {

        AccountId accountId = account.getId().orElseThrow(() ->
                new IllegalStateException("expected account ID not to be empty"));

        List<Activity> activities = account.getActivityWindow().activities();

        // 소유자 검사를 매핑·저장보다 먼저 한다. Activity 하나만으로는 표현할 수 없어
        // 도메인이 강제하지 않는 규칙이라(Activity의 Javadoc 참고) 여기가 강제할 자리다.
        for (Activity activity : activities) {
            if (!accountId.equals(activity.ownerAccountId())) {
                throw new IllegalArgumentException(String.format(
                        "expected activity owner to be account %s but was %s",
                        accountId.value(),
                        activity.ownerAccountId().value()));
            }
        }

        // 매핑을 전부 끝낸 뒤에 저장한다. 매핑이 실패할 수 있으므로(금액이 컬럼 범위를
        // 넘으면 longValueExact가 던진다) 루프에서 매핑과 저장을 번갈아 하면 앞선
        // 활동만 저장된 채로 끝날 수 있다.
        List<ActivityJpaEntity> newActivities = activities.stream()
                .filter(activity -> activity.id() == null)
                .map(accountMapper::mapToJpaEntity)
                .toList();

        activityRepository.saveAll(newActivities);
    }
}
