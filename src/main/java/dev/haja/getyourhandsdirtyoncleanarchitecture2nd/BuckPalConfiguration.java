package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service.MoneyTransferProperties;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.GetAccountBalanceUseCase.GetAccountBalanceQuery;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyCommand;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ElementDescriptor;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static java.util.Objects.requireNonNull;
import static org.springframework.aot.hint.MemberCategory.ACCESS_DECLARED_FIELDS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS;

@Configuration
@EnableConfigurationProperties(BuckPalConfigurationProperties.class)
@ImportRuntimeHints(BuckPalConfiguration.ValidationRuntimeHints.class)
public class BuckPalConfiguration {
    /**
     * 송금 한도를 {@code Money}로 감싼다.
     * <p>
     * 한도가 null이 아니라는 보장은 {@code BuckPalConfigurationProperties}의 {@code @NotNull}이
     * 먼저 맡는다. 여기서 다시 검사하는 것은 그 검증이 돌지 않았을 때를 위해서다 —
     * {@code Money.of(long)}이 {@code Long}을 언박싱하므로, 그대로 두면 어느 설정이 빠졌는지
     * 알 수 없는 NPE가 난다.
     */
    @Bean
    public MoneyTransferProperties moneyTransferProperties(BuckPalConfigurationProperties buckPalConfigurationProperties) {
        Long transferThreshold = requireNonNull(buckPalConfigurationProperties.transferThreshold(),
                "buckpal.transferThreshold must not be null");
        return new MoneyTransferProperties(Money.of(transferThreshold));
    }

    /**
     * 서비스가 현재 시각을 직접 읽지 않고 주입받게 하는 빈. 시간대는
     * {@code LocalDateTime.now()}가 쓰던 기본 시간대 그대로다.
     * <p>
     * 시각을 <b>마이크로초 단위로 끊는다.</b> 활동의 timestamp 컬럼이 {@code timestamp(6)}이라
     * 나노초 자리가 있으면 저장되며 반올림되고(H2는 half-up), 그러면 방금 저장한 활동을 다시
     * 읽었을 때 시각이 달라져 {@code equals}가 깨진다. 정밀도를 저장소에 맞춰 두면 왕복이
     * 손실 없이 끝난다.
     * <p>
     * 반대편 짝은 {@code ActivityJpaEntity}의 {@code secondPrecision = 6}이고,
     * {@code AccountPersistenceAdapterTest.hasMicrosecondTimestampColumn}이 스키마에
     * 남은 정밀도를 고정한다. 다만 그 테스트가 실제로 고정하는 것은 <b>컬럼 정밀도가
     * 여기의 tick(1μs)을 담을 수 있다</b>는 것까지다 — H2 방언 기본값이 이미
     * {@code timestamp(6)}이라 엔티티의 애노테이션을 지워도 그 단언은 통과한다.
     * 애노테이션은 정밀도를 방언에 맡기지 않겠다는 명시이고, 그 값어치는 방언이
     * 바뀌는 날 드러난다. 여기의 tick과 그쪽의 정밀도는 함께 움직여야 한다.
     */
    @Bean
    public Clock clock(){
        return Clock.tick(Clock.systemDefaultZone(), Duration.of(1, ChronoUnit.MICROS));
    }

    /**
     * 스스로 검증하는 입력 모델(커맨드/쿼리)과 그 커스텀 검증기의 리플렉션 힌트를 등록한다.
     * <p>
     * 네이티브 이미지에서 Hibernate Validator는 검증기를 리플렉션으로 생성하고 입력 모델의
     * 필드를 리플렉션으로 읽는다. Spring의 {@code BeanValidationBeanRegistrationAotProcessor}가
     * 같은 힌트를 등록해 주지만 <b>빈 클래스</b>만 훑는다 — 입력 모델은 빈이 아니라
     * {@code common.validation.Validation}으로 스스로 검증하므로 그 시야 밖이다. 힌트가 없으면
     * 네이티브에서 커맨드를 만드는 순간 {@code HV000064}로 실패한다(이슈 #38).
     * <p>
     * 등록하는 멤버 종류는 그 처리기와 같다 — 입력 모델은 필드 접근, 검증기는 생성자 호출이다.
     * 검증기는 손으로 나열하지 않고 AOT 처리 중에 입력 모델의 제약 메타데이터에서 뽑는다.
     * <b>새 커맨드/쿼리를 만들면 {@link #SELF_VALIDATING_TYPES}에 추가할 것.</b> 빠뜨려도 JVM에서는
     * 멀쩡하고 네이티브에서만 실패한다.
     */
    static class ValidationRuntimeHints implements RuntimeHintsRegistrar {

        static final List<Class<?>> SELF_VALIDATING_TYPES = List.of(
                SendMoneyCommand.class,
                GetAccountBalanceQuery.class);

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            try (ValidatorFactory factory = buildDefaultValidatorFactory()) {
                for (Class<?> type : SELF_VALIDATING_TYPES) {
                    hints.reflection().registerType(type, ACCESS_DECLARED_FIELDS);

                    constraintsOf(factory.getValidator().getConstraintsForClass(type))
                            .flatMap(constraint -> constraint.getConstraintValidatorClasses().stream())
                            .forEach(validator ->
                                    hints.reflection().registerType(validator, INVOKE_DECLARED_CONSTRUCTORS));
                }
            }
        }

        /**
         * 클래스 레벨 제약({@code @DistinctAccounts})과 프로퍼티 제약({@code @PositiveMoney})을
         * 합성 제약까지 펼쳐 모은다. 내장 제약({@code @NotNull})은 검증기 목록이 비어 있어
         * 힌트에 아무것도 더하지 않는다.
         */
        private static Stream<ConstraintDescriptor<?>> constraintsOf(BeanDescriptor bean) {
            return Stream.concat(Stream.of(bean), bean.getConstrainedProperties().stream())
                    .map(ElementDescriptor::getConstraintDescriptors)
                    .flatMap(Set::stream)
                    .flatMap(ValidationRuntimeHints::withComposingConstraints);
        }

        private static Stream<ConstraintDescriptor<?>> withComposingConstraints(ConstraintDescriptor<?> constraint) {
            return Stream.concat(Stream.of(constraint),
                    constraint.getComposingConstraints().stream()
                            .flatMap(ValidationRuntimeHints::withComposingConstraints));
        }
    }
}
