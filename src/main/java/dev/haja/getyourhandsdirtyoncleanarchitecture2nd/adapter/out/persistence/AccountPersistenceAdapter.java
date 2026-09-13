package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Activity;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.AccountNotFoundException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.LoadAccountPort;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.UpdateAccountStatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 계좌를 읽고 쓰는 영속성 어댑터.
 * <p>
 * 이 어댑터는 <b>자기 트랜잭션을 갖지 않는다.</b> 트랜잭션 경계는 유스케이스의 것이고
 * ({@code SendMoneyService}의 {@code @Transactional}) 어댑터는 거기에 참여할 뿐이다.
 * 원자성은 이미 확보되어 있다 — {@code updateActivities}는 검사와 매핑을 모두 끝낸 뒤
 * {@code saveAll} 한 번으로 저장하므로 부분 저장이 남을 자리가 없다.
 * <p>
 * 어댑터에 트랜잭션 경계를 두면 오히려 부수 효과가 생긴다. 참여 중인 서비스 트랜잭션에서
 * {@code loadAccount}가 {@code AccountNotFoundException}을 던지면 그것이 어댑터의
 * 트랜잭션 경계를 통과하며 바깥 트랜잭션이 rollback-only로 표시되고, 서비스가 그 예외를
 * 번역해 정상적으로 응답하려 해도 커밋할 수 없게 된다.
 */
@Component
@RequiredArgsConstructor
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
                        .orElseThrow(() -> new AccountNotFoundException(accountId));

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
