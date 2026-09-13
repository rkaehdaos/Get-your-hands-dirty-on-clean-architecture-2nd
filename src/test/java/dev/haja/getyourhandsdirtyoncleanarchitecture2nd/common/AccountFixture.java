package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;

import java.time.LocalDateTime;

/**
 * DB 픽스처 스크립트가 만드는 계좌. 스크립트 경로와 그 안의 계좌 ID, 그 원장을
 * 읽을 때 쓰는 기준일을 한곳에 모아 어댑터 테스트와 시스템 테스트가 같은 값을 보게 한다.
 * <p>
 * 테스트 데이터 빌더({@link AccountTestData})가 쓰는 ID와는 별개다 — 그쪽은 DB 없이
 * 도메인 객체를 만들 때 쓰는 값이다.
 */
public class AccountFixture {

    /**
     * 픽스처 스크립트의 클래스패스 절대 경로. {@code @Sql}의 값은 컴파일 상수여야 하므로
     * 문자열로 둔다.
     */
    public static final String SQL = "/sql/accounts.sql";

    public static final AccountId SOURCE_ACCOUNT_ID = new AccountId(1L);
    public static final AccountId TARGET_ACCOUNT_ID = new AccountId(2L);

    /**
     * 픽스처의 원장을 조회할 때 쓰는 기준 시각. 스크립트의 2018년 활동은 baseline
     * 잔액으로 합산되고({@code timestamp < until}), 2019년 활동과 테스트가 새로 만든
     * 활동은 ActivityWindow로 적재된다({@code timestamp >= since}). 두 경로가 모두
     * 실행되는 자리라 이 값이 스크립트의 활동 시각과 함께 움직여야 한다.
     * <p>
     * {@code now()}로 조회하면 모든 활동이 baseline으로 흘러가 윈도우가 비어 버려,
     * 두 경로 중 하나가 검증되지 않은 채 남는다.
     */
    public static final LocalDateTime BASELINE_DATE = LocalDateTime.of(2019, 1, 1, 0, 0);
}
