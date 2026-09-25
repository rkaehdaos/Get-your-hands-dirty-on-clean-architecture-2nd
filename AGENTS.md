# AGENTS.md

이 저장소에서 작업하는 코딩 에이전트(Claude Code 등)를 위한 안내다. 코드와 Javadoc으로 알 수 있는 것은 되풀이하지 않고, **코드만 봐서는 알 수 없는 규칙과 그 이유**를 적는다.

## 프로젝트

『만들면서 배우는 클린 아키텍처(Get Your Hands Dirty on Clean Architecture)』 2판을 따라 육각형 아키텍처 예제(BuckPal)를 구현하는 학습용 저장소다. 책의 예제를 그대로 옮기지 않고 **최신 Java/Spring으로 다시 쓰는 것**이 핵심이다(아래 "도메인 모델 — 책과 다른 점").

진행 상태: 도메인 모델 → 유스케이스 → 웹 어댑터 → 영속성 어댑터 구현은 끝났다. 테스트는 11장("아키텍처 요소 테스트")에 이르러 기존 테스트를 전부 지우고(`b23ce59`, #12) 책의 흐름대로 다시 쌓는 중이다. 아직 없는 테스트: `Money`·`Activity` 단위 테스트, `GetAccountBalanceService` 테스트.

### 구성 요소

- **인바운드 포트** `SendMoneyUseCase`, `GetAccountBalanceUseCase`. 유스케이스 실패 예외(`ThresholdExceededException`, `InsufficientFundsException`, `NoSuchAccountException`)도 `port/in`에 있다.
- **아웃바운드 포트** `LoadAccountPort`, `UpdateAccountStatePort`, `AccountLock`. `LoadAccountPort`가 던지는 `AccountNotFoundException`은 `port/out`에 있다.
- **서비스** `SendMoneyService`(`@UseCase` + `jakarta.transaction.Transactional`), `GetAccountBalanceService`. **`GetAccountBalanceService`는 빈이 아니다** — 이 유스케이스를 쓰는 인바운드 어댑터가 아직 없어서다(책 원본도 같다). `@UseCase`는 그 어댑터를 추가하는 `feat` 커밋에서 붙이고, 트랜잭션 경계도 그때 정한다. 그 전까지 쓰려면 `new`나 `@Import`로 조립한다. 둘이 공유하는 헬퍼 `AccountLoader`가 서비스 패키지에 있다.
- **인바운드 어댑터** `SendMoneyController`, `SendMoneyExceptionHandler`(`@RestControllerAdvice`).
- **아웃바운드 어댑터** `AccountPersistenceAdapter`(`LoadAccountPort`·`UpdateAccountStatePort`를 함께 구현), `NoOpAccountLock`(아무것도 잠그지 않는 자리표시자).
- **설정**(루트 패키지) `BuckPalConfiguration` — `MoneyTransferProperties`·`Clock` 빈을 등록한다. 네이티브용 `ValidationRuntimeHints`가 중첩돼 있고, 등록은 `META-INF/spring/aot.factories`가 맡는다. `BuckPalConfigurationProperties` — `buckpal.*` 바인딩. 아래 "설정"·"네이티브 이미지" 참고.

### 패키지 구조

책과 같다.

```
adapter/in/web              인바운드 웹 어댑터
adapter/out/persistence     아웃바운드 영속성 어댑터(JPA 엔티티 · 리포지토리 · 매퍼 · NoOpAccountLock)
application/domain/model    도메인 모델
application/domain/service  유스케이스 구현(서비스) + MoneyTransferProperties
application/port/in         인바운드 포트 + 입력 모델(커맨드/쿼리) + 커스텀 제약 + 유스케이스 실패 예외
application/port/out        아웃바운드 포트
common                      계층 식별 스테레오타입(@WebAdapter · @UseCase · @PersistenceAdapter)
common/validation           Validation 헬퍼 (application 바깥)
```

- 루트 패키지에는 `BuckPalApplication`, `BuckPalConfiguration`, `BuckPalConfigurationProperties`만 둔다.
- `common`의 스테레오타입은 표지가 아니라 **`@Component`를 메타 애노테이션으로 가진 빈 등록**이다. 빈이 아니던 클래스에 붙이면 동작 변경이다(`refactor`가 아니다). `@WebAdapter`는 `@Controller`가 아니므로 `@RestController`를 대신하지 못한다 — 빼면 핸들러로 인식되지 않으니 둘을 함께 붙인다.
- `src/test`의 `common` 패키지(테스트 데이터 빌더·픽스처 상수)는 `src/main`의 `common`(스테레오타입)과 **같은 패키지를 소스셋만 나눠 쓴다.** 역할은 서로 무관하다.
- `@Sql` 픽스처 스크립트는 패키지 구조를 따르지 않고 `src/test/resources/sql/`에 모은다(현재 `accounts.sql` 하나).
- 들여쓰기·줄바꿈·인코딩은 루트 `.editorconfig`가 고정한다(Java는 공백 4칸).

## 명령어

```bash
./gradlew build                        # 컴파일 + 테스트 + 패키징
./gradlew test                         # 전체 테스트
./gradlew test --tests '*AccountTest*' # 단일 테스트 클래스
./gradlew bootRun                      # 애플리케이션 실행
./gradlew nativeCompile                # GraalVM 네이티브 이미지 빌드
./gradlew nativeTest                   # 네이티브 이미지에서 테스트 실행
```

JDK는 `mise.toml`(`.gitignore` 대상)이 `oracle-graalvm-25.0.4.1.1`을 지정한다. Gradle toolchain은 Java 25를 요구한다.

## 스택 주의점

- **Spring Boot 4.1.1** — 스타터 이름이 3.x와 다르다(`spring-boot-starter-webmvc`, 테스트는 `spring-boot-starter-test` 대신 `spring-boot-starter-data-jpa-test` / `spring-boot-starter-webmvc-test`). 의존성을 3.x 시절 기억으로 적지 말 것.
- **테스트 슬라이스의 패키지도 옮겨졌다.** 임포트를 기억으로 적지 말 것.
  - `@DataJpaTest` → `org.springframework.boot.data.jpa.test.autoconfigure`
  - `TestEntityManager` → `org.springframework.boot.jpa.test.autoconfigure`
  - `@WebMvcTest` → `org.springframework.boot.webmvc.test.autoconfigure`
  - `TestRestTemplate` / `@AutoConfigureTestRestTemplate` → `org.springframework.boot.resttestclient`(+`.autoconfigure`)
  - 그대로인 것: `@SpringBootTest`(`org.springframework.boot.test.context`), `@MockitoBean`(`org.springframework.test.context.bean.override.mockito`)
- **H2는 `testAndDevelopmentOnly`다.** `testRuntimeOnly`면 `bootRun`이 `Failed to configure a DataSource`로 뜨지 않고, `runtimeOnly`면 `bootJar`에 실려 datasource 설정이 빠진 프로덕션이 실패 대신 빈 인메모리 DB로 조용히 뜬다. `application.yml`에 datasource 설정이 없어 `bootRun`·테스트 모두 내장 DB를 자동 구성한다. 스키마는 Hibernate `ddl-auto`에 맡기고 전역 `schema.sql`/`data.sql`은 두지 않는다(테스트별 `@Sql` 픽스처만 쓴다). `bootRun`의 DB는 비어 있다.
- **직접 임포트하는 라이브러리는 전이 의존성에 기대지 않고 명시 선언한다**(버전은 BOM에 맡긴다). `mockito-junit-jupiter`, `spring-boot-resttestclient`가 그래서 있다.
- `testImplementation("org.springframework.boot:spring-boot-restclient")`는 **`processTestAot`에 필요하다** — 빼면 `ClassNotFoundException: ...RestTemplateBuilder`. 지우지 말 것.
- Bean Validation(`spring-boot-starter-validation`)은 컨텍스트 없이도 쓰므로 `implementation`이다.
- Gradle 9.8.0 / Kotlin DSL, Hibernate ORM 플러그인(bytecode enhancement), GraalVM Native Build Tools.
- **Gradle을 올릴 때 `distributionUrl`을 손으로 고치지 말 것** — `distributionSha256Sum` 불일치로 실패하고 래퍼 jar·스크립트도 갱신돼야 한다. `./gradlew wrapper --gradle-version <버전> --distribution-type bin --gradle-distribution-sha256-sum <services.gradle.org의 .sha256 값>`으로 바꾼다.
- JUnit 5, AssertJ, Mockito(BDD 스타일).

## 코드 컨벤션

### 도메인 모델 — 책과 다른 점

책은 Lombok `@Value`/가변 클래스를 쓰지만 이 저장소는 **가능한 곳에 record로 불변 모델**을 만든다. 값 객체·단순 불변 데이터(`Activity`, `ActivityWindow`, `Money`, ID 값 객체)는 record, `Account`처럼 record가 맞지 않는 모델은 Lombok 클래스다.

- `ActivityWindow.addActivity()`는 리스트를 바꾸지 않고 **새 `ActivityWindow`를 반환**한다. compact constructor에서 `List.copyOf()`로 방어적 복사한다.
- ID는 중첩 record 값 객체다(`Account.AccountId`, `Activity.ActivityId`). ID가 없는 신규 엔티티는 `id`가 `null`인 보조 생성자로 만든다.
- **도메인은 현재 시각을 읽지 않는다.** `Account.withdraw`/`deposit`은 시각을 `LocalDateTime` 인자로 받는다. `Clock`조차 모르고, 시각은 서비스가 고른다.
- **record에는 Lombok을 쓰지 않는다**(record + compact constructor + `requireNonNull`). 도메인 계층 전반의 금지가 아니다 — record가 아닌 `Account`에는 Lombok을 쓴다.

### 도메인 모델 record의 null 검증

`application/domain/model`에만 해당한다(입력 모델은 Bean Validation — 아래 참고).

- **compact constructor에 모은다.** 형식은 `requireNonNull(<컴포넌트>, "<컴포넌트명> must not be null")`이고 **static import**로 쓴다.
- **메시지 인자를 반드시 넘긴다.** `List.of`/`List.copyOf`의 NPE 메시지는 비어 있거나 helpful NPE(JVM 옵션 의존)라 계약이 될 수 없다.
- 본문 순서는 **검증 → 정규화**(`List.copyOf` 등)다.
- `this(...)`로 값을 **변환 없이** 넘기는 보조 생성자에는 검증을 중복하지 않는다(`Activity`의 5-arg 생성자). 단 **`this(...)`의 인자식은 정규 생성자보다 먼저 평가되므로**, 인자식에서 입력을 변환하면 그 자리에서 `requireNonNull`로 감싼다(`ActivityWindow`의 가변인자 생성자 — `List.of(배열)`이 먼저 `배열.length`를 읽는다).
- 의도적으로 nullable인 컴포넌트는 검증하지 않고 주석으로 의도를 남긴다(`Activity.id`).
- **ID 값 객체의 `value`는 null일 수 없다.** "id 없음"은 ID 참조 자체가 null인 것으로만 표현한다. `AccountId(null)`을 허용하면 그것끼리 `equals`가 `true`가 되어 서로 다른 미영속 계좌의 잔액이 합산된다.
- 배열 → 리스트는 `Arrays.asList` 대신 `List.of`다(이어지는 `List.copyOf`가 복사 없이 그대로 반환한다).

### 유스케이스 입력 모델 (커맨드/쿼리)

**record + Bean Validation으로 자기 검증**한다(`SendMoneyCommand`, `GetAccountBalanceUseCase.GetAccountBalanceQuery`). 책의 최종형만 쓴다.

- **compact constructor가 아니라 정규 생성자를 명시적으로 선언**하고, 필드를 대입한 뒤 `validate(this)`를 부른다. compact constructor에서는 필드가 아직 대입되지 않아 전부 null로 보인다.

  ```java
  public SendMoneyCommand(AccountId sourceAccountId, AccountId targetAccountId, Money money) {
      this.sourceAccountId = sourceAccountId;
      this.targetAccountId = targetAccountId;
      this.money = money;

      validate(this);
  }
  ```

- 제약은 record 헤더의 컴포넌트에 붙인다. null 검증은 `@NotNull`(`requireNonNull` 아님).
- `validate`는 `common.validation.Validation.validate`의 static import다. `ValidatorFactory`를 한 번만 만들고 **닫지 않는다**(닫으면 만든 `Validator`를 쓸 수 없다).
- 새 커맨드/쿼리는 **유스케이스 인터페이스 안의 중첩 record**로 만든다. top-level인 `SendMoneyCommand`는 먼저 만들어진 예외다.
- **입력 모델은 `port/in`에 둔다** — 네이티브용 리플렉션 힌트가 그 패키지를 스캔해 등록된다. **`@Valid` 캐스케이드와 컨테이너 원소 제약(`List<@NotNull Money>`)은 쓰지 않는다** — 레지스트라가 거부한다. 아래 "네이티브 이미지" 참고.

#### 커스텀 제약

표준 제약으로 안 되는 규칙은 애노테이션 + `ConstraintValidator` 쌍으로 `port/in`에 둔다(`PositiveMoney`, `DistinctAccounts`).

- 애노테이션: `message()`/`groups()`/`payload()` 세 속성 전부, `@Target`, `@Retention(RUNTIME)`, `@Constraint(validatedBy = ...)`, `@Documented`.
- **단일 필드 규칙**은 `@Target({FIELD})`(`PositiveMoney`). record 컴포넌트의 애노테이션은 필드로 전파되므로 `RECORD_COMPONENT`는 필요 없다.
- **교차 필드 규칙**은 `@Target({TYPE})`의 클래스 레벨 제약으로 record 선언에 붙이고, 검증기 타입도 커맨드 자체다(`ConstraintValidator<DistinctAccounts, SendMoneyCommand>`). 프로퍼티 경로가 비어 메시지가 `": ..."`로 시작하므로 `disableDefaultConstraintViolation()` + `buildConstraintViolationWithTemplate(...).addPropertyNode("<컴포넌트명>")`로 `"<컴포넌트명>: <메시지>"` 꼴을 맞춘다.
- 메시지에서 값을 보여줄 때는 `${validatedValue}`(EL)다. `{validatedValue}`는 보간되지 않는다.
- **검증기는 null을 유효로 본다** — null은 `@NotNull`의 몫이다(`@NotNull @PositiveMoney`). 클래스 레벨 검증기도 필드가 null이면 유효로 봐야 위반이 두 번 보고되지 않는다.
- **입력 모델의 제약은 도메인 불변식이 아니다.** `@PositiveMoney`는 "이 커맨드의 금액은 양수"라는 뜻이고, `Money`는 잔액·출금 때문에 0과 음수를 표현해야 한다. 이 구분을 애노테이션 Javadoc에 적는다.

### 서비스 (유스케이스 구현)

- **package-private** 클래스이고 포트 인터페이스로만 노출한다. 의존성은 `private final` + `@RequiredArgsConstructor`.
- **현재 시각은 주입받은 `Clock`에서 얻는다. `LocalDateTime.now()`를 직접 부르지 말 것.** `SendMoneyService`는 시각을 **한 번만 읽어** baselineDate(`minusDays(10)`)와 출금·입금 활동에 함께 쓴다 — 한 이체의 두 면이라 같은 시각이어야 한다.
- 입력 검증을 다시 하지 않는다(커맨드가 스스로 한다). 설정값과 비교해야 하는 정책(송금 한도)만 서비스가 검사해 `port/in` 예외를 던진다.
- 포트의 예외는 감싸지 않고 전파한다. **예외는 `AccountNotFoundException` 하나** — `AccountLoader`가 `port/in`의 `NoSuchAccountException`으로 번역한다. "계좌 없음"은 유스케이스의 거부이고, 인바운드 어댑터가 `port/out`을 import하지 않게 하기 위해서다.
- **유스케이스의 거부는 반환값이 아니라 `port/in` 예외다.** `sendMoney`는 `void`이고, 도메인의 `boolean`(`Account.withdraw`/`deposit`, 이 계약은 그대로다)이 실패를 알리면 서비스가 예외로 번역한다. HTTP로의 번역은 웹 어댑터의 몫이다.
- 잠금은 `AccountLock`으로 하고 **`try`/`finally`로 해제를 보장**한다. 출금 → 입금 계좌 순으로 중첩해 잠그고, 잡지 못한 잠금은 풀지 않는다. 전역 획득 순서를 보장하지 않으므로 **교착 회피는 구현체 책임**이다(`AccountLock` Javadoc).

### 웹 어댑터 — 실패의 HTTP 표현

`SendMoneyExceptionHandler`가 모두 **`ProblemDetail`(RFC 9457)** 로 매핑하고, `SendMoneyControllerTest`가 이 매핑을 고정한다.

| 예외 | 상태 | 의미 |
|---|---|---|
| `ThresholdExceededException`, `InsufficientFundsException` | 422 Unprocessable Content | 이체가 거부됨 |
| `NoSuchAccountException` | 404 Not Found | 요청한 자원이 없음 |
| `ConstraintViolationException` | 400 Bad Request | 요청 자체가 잘못됨(금액 ≤ 0, 자기 이체 `@DistinctAccounts` 등) |

커맨드 검증 예외는 컨트롤러가 커맨드를 만드는 자리에서 나므로 인자 바인딩이 아니라 `@ExceptionHandler`에 닿는다.

### 영속성 어댑터

- **패키지 밖으로 새는 타입이 없다** — 엔티티·리포지토리·매퍼·어댑터가 전부 package-private이다.
- JPA 엔티티는 도메인과 별개인 Lombok 클래스다: `@Data` + `@EqualsAndHashCode(of = {"id"})` + `@AllArgsConstructor` + `@NoArgsConstructor(access = PROTECTED)`. 값 객체는 풀어서 컬럼으로 담고, 애노테이션은 개별 임포트, `@Table(name = ...)`은 소문자다.
- **스키마의 유일한 출처가 엔티티다**(`ddl-auto`). `ActivityJpaEntity`의 컬럼은 `@Column(nullable = false, updatable = false)`(원장은 append-only)이고, **`id`만 `Long`, 나머지는 원시 타입**이다 — 박싱이면 null이 어느 컬럼인지 모를 언박싱 NPE로 드러난다. `id`는 `@GeneratedValue`가 null에 의존한다.
- 인덱스도 엔티티에 적는다. **`columnList`는 물리 컬럼명이 아니라 프로퍼티명**이다. 틀려도 DDL 오류가 로그로만 남으므로 존재를 테스트로 고정한다(`hasOwnerTimestampIndex`).
- **`Money` → `long`은 `longValueExact()`다.** `longValue()`는 범위 밖 값을 조용히 잘라낸다. 저장 실패가 잘린 금액보다 낫다.
- **합계 조회는 `BigInteger`로 읽는다.** 기준 잔액은 입금·출금을 `case`로 가른 단일 `@Query`(텍스트 블록)이고, `cast(... as BigInteger)`를 **`sum` 안쪽**에 둔다 — 바깥에 두면 H2가 넓힌 NUMERIC을 드라이버가 `getLong()`으로 읽다 long 범위에서 실패한다. 결과는 별칭 기반 인터페이스 프로젝션(`BaselineBalanceView`, 별칭 = getter 이름)이고, 행이 없어도 한 행이 오므로 `coalesce(..., 0)`.
- 도메인 ↔ 엔티티 변환은 `AccountMapper`가 전담한다. 관련 아웃바운드 포트는 한 어댑터가 함께 구현한다.
- **`updateActivities`의 계약 셋**(원본은 `UpdateAccountStatePort` Javadoc): ① `id == null`인 활동만 저장하고 생성된 id를 되돌리지 않는다 — **같은 `Account`로 두 번 부르면 두 번 저장된다.** ② 저장 전 모든 활동의 `ownerAccountId`가 계좌 id와 같은지 검사한다(다르면 `IllegalArgumentException`, 계좌 id가 없으면 `IllegalStateException`). ③ **검사·매핑을 모두 끝낸 뒤 `saveAll`** — 번갈아 하면 부분 저장이 남는다.
- **트랜잭션 경계는 서비스에 있다. 어댑터에 `@Transactional`을 두지 말 것** — `loadAccount`의 `AccountNotFoundException`이 그 경계를 지나며 서비스 트랜잭션을 rollback-only로 만든다.

### 설정 (`buckpal.*`)

- **송금 한도 `buckpal.transferThreshold`는 `application.yml`이 유일한 출처다. 코드에 기본값을 되살리지 말 것** — relaxed binding은 모르는 키를 무시하므로, 기본값이 있으면 오타 하나로 코드의 값이 조용히 한도가 된다. 한도를 두지 않으려면 그 값을 yml에 명시한다.
- `BuckPalConfigurationProperties`는 `@Validated` record(`@NotNull @Positive Long`)라 누락·0 이하에서 기동이 실패한다. `ignoreUnknownFields = false`라 **`buckpal.*` 아래 모르는 키도 기동을 막는다** — 기본 yml에 값이 늘 있으니, 이것이 없으면 프로파일 yml·환경변수 쪽 키 오타가 무시된다.
- `buckpal` 아래에 키를 추가하려면 record에 컴포넌트부터 추가한다. 환경변수는 `BUCKPAL_TRANSFERTHRESHOLD`다(`BUCKPAL_TRANSFER_THRESHOLD`는 모르는 키).
- `MoneyTransferProperties`는 null만 검증하고 기본값 생성자가 없다.
- `Clock` 빈은 `Clock.tick(systemDefaultZone(), 1μs)`다. `ActivityJpaEntity.timestamp`의 `secondPrecision = 6`과 **함께 움직여야** 저장 후 다시 읽은 시각이 같다.

### 네이티브 이미지

- **입력 모델의 리플렉션 힌트는 우리 레지스트라가 등록한다.** Spring의 `BeanValidationBeanRegistrationAotProcessor`는 빈 클래스만 훑는데 커맨드/쿼리는 빈이 아니다. 힌트가 없으면 네이티브에서 커맨드를 만드는 순간 `HV000064`로 실패한다(송금 API가 500).
  - `ValidationRuntimeHints`가 AOT 처리 중에 `application.port.in`을 스캔해 **제약이 하나라도 붙은 record**를 입력 모델로 본다. 그 필드(`ACCESS_DECLARED_FIELDS`)와, 제약에서 뽑은 검증기의 생성자(`INVOKE_DECLARED_CONSTRUCTORS`)를 등록한다. 입력 모델도 검증기도 손으로 나열하지 않는다. **대신 입력 모델을 `port/in` 밖에 두면 힌트가 빠지고, JVM 테스트로는 드러나지 않는다.**
  - 등록은 `@ImportRuntimeHints`가 아니라 `META-INF/spring/aot.factories`다. 애노테이션이면 `BuckPalConfiguration`을 담은 컨텍스트가 AOT 처리될 때만 기여해, 컨텍스트 없이 도는 `SendMoneyCommandTest`의 네이티브 실행이 풀 컨텍스트 테스트의 부수효과에 기대게 된다.
  - `@Valid` 캐스케이드와 컨테이너 원소 제약은 따라가지 않고 `IllegalStateException`으로 거부한다. 조용히 힌트를 빠뜨리는 대신 JVM 테스트와 AOT 빌드에서 실패한다. 필요해지면 레지스트라를 Spring 처리기처럼 재귀로 확장한다.
- **네이티브에서 돌 수 없는 테스트는 `@DisabledInNativeImage`를 붙이고, 그 빈자리를 누가 메우는지 주석으로 남긴다.** 해당하는 것: Mockito(`Mockito.mock`, `@MockitoBean` — 런타임 바이트코드 생성 불가), `ApplicationContextRunner`(런타임 설정 처리·JDK 프록시), AOT 빌드 시점 코드(`RuntimeHintsRegistrar` — 네이티브 안에서는 클래스패스를 스캔할 수 없다).

  | 제외된 테스트 | 네이티브에서 대신 덮는 테스트 |
  |---|---|
  | `SendMoneyServiceTest`, `SendMoneyControllerTest` | `SendMoneySystemTest`(송금 **성공** 경로만 — 서비스의 실패 분기와 실패 응답의 `ProblemDetail` 매핑은 네이티브에서 실행되지 않는다), `SendMoneyCommandTest`(`PositiveMoneyValidator`를 실행하고 `${validatedValue}` 보간 결과까지 단언하는 유일한 테스트) |
  | `BuckPalConfigurationPropertiesTest` | `BuckPalConfigurationPropertiesValidationTest`(컨텍스트의 `Validator`로 기동과 같은 바인딩 경로를 밟는다) |
  | `ValidationRuntimeHintsTest` | 없음 — 빌드 시점 코드다. 등록된 힌트는 `SendMoneyCommandTest`·`SendMoneySystemTest`가 커맨드를 만들며 쓴다 |

- **스프링 컨텍스트를 띄우는 테스트라면 `@DisabledInAotMode`도 함께 붙인다.** `@DisabledInNativeImage`는 실행만 막고, 그 컨텍스트는 `processTestAot`에서 여전히 AOT 처리되어 이미지에 실린다. 같은 컨텍스트를 쓰는 다른 테스트가 있으면 그쪽에도 붙여야 한다(`@DisabledInAotMode` Javadoc).
- 힌트 등록 자체는 `ValidationRuntimeHintsTest`가 JVM `test`에서 고정한다(스캔 결과, `aot.factories` 등록, 미지원 제약 거부) — `nativeTest`보다 먼저 드러난다.

## 테스트

### 스타일

삭제 전 테스트의 스타일(한글 클래스·메서드명, `@Nested`)은 **되살리지 않는다.** 책의 스타일을 따른다.

- `@Nested` 없는 평평한 `@Test`, **영문 메서드명**(`calculatesBalance()`, `withdrawalSucceeds()`).
- 시나리오가 복잡하면 `given…_then…` 꼴 메서드명에 한글 `@DisplayName`을 붙인다(`givenNoSuchAccount_thenRespondsWithNotFound` + `@DisplayName("계좌가 없으면 404 Not Found가 응답됨")`). 메서드명으로 충분하면 붙이지 않는다.
- `// given` / `// when` / `// then` 주석으로 구분하고 주석 앞뒤에 빈 줄을 둔다.
- 단언은 AssertJ(`assertThat`, `assertThatThrownBy(...).isInstanceOf(...)`). 우리 코드의 예외임을 고정할 때는 `.hasMessageContaining("<컴포넌트명>")`만 덧붙인다 — 문구 전체에 묶이지 않게. JDK 내부 예외에는 메시지 단언을 붙이지 않는다. 컴포넌트명이 helpful NPE에도 나타나 검사 없이도 통과하는 경우에는 **우리 코드만 넣는 문자열(설정 키 등)로 단언**한다(`BuckPalConfigurationTest`).
- 커맨드/쿼리 자기 검증은 `ConstraintViolationException` + `.hasMessageContaining("<컴포넌트명>")`.

### 테스트 데이터 빌더 (`src/test`의 `common`)

- 픽스처는 테스트 클래스 안 팩터리가 아니라 빌더로 만든다(`AccountTestData`, `ActivityTestData`). `defaultAccount()`/`defaultActivity()`(static import)가 모든 필드를 채운 빌더를 주고, 테스트는 관심 필드만 `withXxx(...)`로 덮어써 `build()`한다. 빌더는 평범한 가변 클래스이고 이름은 영문이다.
- `AccountBuilder.build()`는 `Account.withId(...)`다. id 없는 계좌는 빌더 없이 `Account.withoutId`로 만든다.
- `ActivityBuilder`의 `id`는 기본값이 없다 — 미영속 활동이 기본이고 필요할 때만 `withId(...)`.
- 고정 시각은 `TimeTestData`의 `NOW`와 UTC 고정 `CLOCK`(`LocalDateTime.now(CLOCK) == NOW`)을 static import로 공유한다. 테스트마다 다시 선언하지 말고, `now()` 전후 범위로 `isBetween` 단언하지 말 것.
- DB 픽스처는 `AccountFixture`의 상수로 참조한다(`SQL`, `SOURCE_ACCOUNT_ID`, `TARGET_ACCOUNT_ID`, `BASELINE_DATE`). `@Sql` 값은 컴파일 상수여야 해서 경로만 `String`이다.

### 계층별 기준

- **서비스**: 스프링 컨텍스트 없이, 포트를 **필드에서 `Mockito.mock(...)`으로 만들어** 생성자로 조립한다(`MockitoExtension`·`@Mock`·`@BeforeEach` 안 씀). 테스트마다 다른 협력자(한도)는 그 테스트에서 서비스를 새로 만든다. `Account`도 목으로 세워 `withdraw`/`deposit`/`getId()`를 제어한다. 포트 인자는 `ArgumentCaptor`로 단언한다.
- **Mockito는 BDD 스타일**: `given(...).willReturn(...)`, `then(mock).should()...`, `willThrow(...).given(mock)...`, `shouldHaveNoInteractions()`. `when`/`verify`는 쓰지 않는다.
- **영속성**: `@DataJpaTest` + `@Import({AccountPersistenceAdapter.class, AccountMapper.class})`(슬라이스 스캔 대상이 아니다). 매퍼만이면 `new AccountMapper()`. 읽기 검증은 `@Sql`, 쓰기 검증은 빌더로 만든 도메인 객체.
  - 스키마 검증은 엔티티를 우회한다 — `JdbcTemplate` 직접 insert로 `DataIntegrityViolationException`, 인덱스는 H2 `INFORMATION_SCHEMA.INDEX_COLUMNS` 조회.
  - 커밋 경계를 봐야 하는 테스트만 `@Transactional(propagation = NOT_SUPPORTED)`이고, **그 메서드에는 `@Sql`을 붙이지 않는다**(픽스처가 커밋되어 다른 테스트를 오염시킨다).
- **웹**: `@WebMvcTest(controllers = SendMoneyController.class)` + `MockMvcTester` + `@MockitoBean`. 실패 경로의 HTTP 매핑은 여기서 고정한다.
- **시스템**: `@SpringBootTest(RANDOM_PORT)` + `@AutoConfigureTestRestTemplate` + `TestRestTemplate`. 비싸므로 **주요 경로 하나만** 둔다.

### DB 픽스처 (`sql/accounts.sql`)

어댑터 테스트와 시스템 테스트가 한 파일을 공유한다.

- 활동 ID는 1001부터다 — Hibernate 시퀀스(1부터)와 PK가 겹치지 않게.
- **검증용 조회의 baselineDate는 `AccountFixture.BASELINE_DATE`(2019-01-01)다.** `now()`로 조회하면 모든 활동이 baseline으로 합산되어 `ActivityWindow`가 빈다. 이 날짜로 2018년 행은 baseline, 2019년 행과 새 활동은 윈도우로 갈려 두 경로가 모두 실행된다. 리터럴로 적지 말 것 — 활동 시각을 바꿀 때 한 곳만 고쳐진다.
- **한계선에 걸치지 않게 여유를 둔다.** 출금 계좌 잔액이 1000이라 500을 보내고도 500이 남는다(잔액 = 이체액이면 `>=` → `>` 회귀를 놓친다). `SendMoneySystemTest`의 `// given`에 전제 단언으로 고정돼 있다.
- **시스템 테스트는 롤백되지 않고, `@Transactional`을 붙여서도 안 된다.** `RANDOM_PORT`의 서버 스레드는 별도 트랜잭션이라 커밋 안 된 `@Sql` 데이터를 못 보고 500이 난다. `@SqlConfig(ISOLATED)`로도 서버가 만든 활동은 남는다. 그래서 **스크립트 맨 위의 `delete from activity; delete from account;`가 정리를 맡는다** — 매 테스트가 같은 상태에서 시작한다.

## 커밋

`.gitmessage.txt`가 템플릿이다. 형식은 `<이모지> <type>(<scope>): <subject>`, **subject는 한글**이다(예: `✨ feat(core): ActivityWindow에 addActivity 메서드 추가`). type/scope/이모지는 템플릿에 있는 것만 쓴다.

- scope: 도메인·애플리케이션 계층(포트·서비스·입력 모델) `core`, 웹 어댑터 `api`, 영속성 어댑터 `db`, 설정 `config` — 루트 패키지 설정(`BuckPalConfiguration*`, `application.yml`, `META-INF/spring/aot.factories`)과 저장소·에이전트 설정 파일(`AGENTS.md`, `.editorconfig`, `.githooks`, `.gitmessage.txt`). 문서 변경은 type `📝 docs`가 설정 변경 커밋과 구분한다. 의존성 `deps`(`🔨 build(deps)` / `📦 chore(deps)`).
- **테스트 커밋의 scope는 대상 코드를 따른다**(`✅ test(core)`, `✅ test(config)`). scope `test`는 테스트 코드 자체가 대상일 때다(`♻️ refactor(test): AccountBuilder를 AccountTestData로 이동`, `✅ test(test): 목 기반 테스트를 네이티브 이미지에서 제외`).
- 커밋은 매우 잘게 — 메서드 하나, 검증 하나 수준. 기능과 테스트는 별도 커밋(`feat`/`fix` → `test`).

### 이슈 참조 푸터

브랜치명은 `<이슈번호>-<설명>`이고, 커밋 끝의 `Refs: #<이슈번호>` 푸터로 GitHub 이슈 타임라인에 커밋이 뜬다. `.githooks/commit-msg`가 이 푸터를 자동으로 붙인다(이미 번호가 있거나, 브랜치에 번호가 없거나, 본문이 비었거나, 머지 커밋이면 건너뛴다). **클론 직후 한 번** 켠다:

```bash
git config core.hooksPath .githooks
```

PR은 `.github/PULL_REQUEST_TEMPLATE.md`를 채운다.
