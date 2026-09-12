package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;

/**
 * DB 픽스처 스크립트가 만드는 계좌. 스크립트 경로와 그 안의 계좌 ID를 한곳에 모아
 * 어댑터 테스트와 시스템 테스트가 같은 값을 보게 한다.
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
}
