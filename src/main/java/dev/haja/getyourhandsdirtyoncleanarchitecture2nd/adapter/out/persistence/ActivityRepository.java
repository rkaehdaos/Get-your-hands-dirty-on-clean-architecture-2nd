package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

interface ActivityRepository extends
        JpaRepository<ActivityJpaEntity, Long> {

    @Query("""
            select a from ActivityJpaEntity a
            where a.ownerAccountId = :ownerAccountId
            and a.timestamp >= :since
            """)
    List<ActivityJpaEntity> findByOwnerSince(
            @Param("ownerAccountId") long ownerAccountId,
            @Param("since") LocalDateTime since);

    /**
     * 기준일 이전 활동의 입금·출금 합계를 한 번에 구한다.
     * <p>
     * {@code cast}를 {@code sum} <b>안쪽</b>에 두어 DB와 무관하게 합계의 타입이 BigInteger가
     * 되게 한다. 바깥에 두면 DB가 정한 합계 타입(H2는 BIGINT를 NUMERIC으로 넓힌다)을
     * 드라이버가 {@code getLong()}으로 읽는 순간 long 범위를 넘겨 실패한다.
     * <p>
     * group by 없는 집계라 대상 행이 없어도 한 행이 오고, 그때 합계는 null이므로
     * {@code coalesce}로 0을 채운다.
     */
    @Query("""
            select
                coalesce(sum(cast(case when a.targetAccountId = :accountId then a.amount else 0 end as BigInteger)), 0) as deposits,
                coalesce(sum(cast(case when a.sourceAccountId = :accountId then a.amount else 0 end as BigInteger)), 0) as withdrawals
            from ActivityJpaEntity a
            where a.ownerAccountId = :accountId
            and a.timestamp < :until
            """)
    BaselineBalanceView getBaselineBalanceUntil(
            @Param("accountId") long accountId,
            @Param("until") LocalDateTime until);

    /**
     * 기준 잔액의 두 항. 인터페이스 프로젝션은 <b>별칭</b>으로 값을 찾으므로
     * 쿼리의 {@code as deposits}/{@code as withdrawals}는 getter 이름과 같아야 한다.
     */
    interface BaselineBalanceView {
        BigInteger getDeposits();
        BigInteger getWithdrawals();
    }
}
