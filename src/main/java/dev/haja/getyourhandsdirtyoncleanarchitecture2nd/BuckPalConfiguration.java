package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service.MoneyTransferProperties;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyUseCase;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ElementDescriptor;
import jakarta.validation.metadata.PropertyDescriptor;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

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
     * 이 레지스트라는 {@code @ImportRuntimeHints}가 아니라 {@code META-INF/spring/aot.factories}로
     * 등록한다. 애노테이션으로 붙이면 이 설정 클래스를 담은 컨텍스트가 AOT 처리될 때만 기여한다 —
     * 스프링 컨텍스트 없이 커맨드를 만드는 테스트의 네이티브 실행이 풀 컨텍스트 테스트가 따로
     * 있다는 우연에 기대게 된다. {@code aot.factories}로 등록하면 어느 컨텍스트가 AOT 처리되든
     * 기여한다.
     * <p>
     * 등록하는 멤버 종류는 그 처리기와 같다 — 입력 모델은 필드 접근, 검증기는 생성자 호출이다.
     * 입력 모델도 검증기도 손으로 나열하지 않는다. AOT 처리 중에 {@code application.port.in}을
     * 스캔해 제약이 하나라도 붙은 record를 입력 모델로 보고, 검증기는 그 제약 메타데이터에서
     * 뽑는다. <b>입력 모델을 그 패키지 밖에 두면 힌트가 등록되지 않는다</b> — JVM에서는 멀쩡하고
     * 네이티브에서만 실패한다.
     * <p>
     * {@code @Valid} 캐스케이드와 컨테이너 원소 제약({@code List<@NotNull Money>})은 따라가지
     * 않는다. 그런 입력 모델을 만나면 힌트를 빠뜨리는 대신 {@code IllegalStateException}으로
     * 실패한다 — 네이티브에서 조용히 깨지는 대신 JVM 테스트와 AOT 빌드에서 드러난다. 필요해지면
     * Spring의 처리기처럼 그 타입까지 재귀로 따라가도록 이 레지스트라를 확장할 것.
     */
    static class ValidationRuntimeHints implements RuntimeHintsRegistrar {

        private static final String INPUT_MODEL_PACKAGE = SendMoneyUseCase.class.getPackageName();

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            registerInputModels(hints, recordsIn(INPUT_MODEL_PACKAGE, classLoader));
        }

        /**
         * 후보 중 제약이 붙은 타입을 입력 모델로 보고 힌트를 등록한다. 스캔과 나눠 둔 것은
         * 테스트가 스캔 범위 밖의 픽스처 타입으로 부르기 위해서다.
         */
        static void registerInputModels(RuntimeHints hints, List<Class<?>> candidates) {
            try (ValidatorFactory factory = buildDefaultValidatorFactory()) {
                Validator validator = factory.getValidator();
                candidates.stream()
                        .map(validator::getConstraintsForClass)
                        .filter(BeanDescriptor::isBeanConstrained)
                        .forEach(bean -> register(hints, bean));
            }
        }

        /**
         * 패키지 아래의 record를 모두 찾는다. 유스케이스 인터페이스에 중첩된 record도 암묵적으로
         * static이라 후보가 된다.
         */
        private static List<Class<?>> recordsIn(String basePackage, ClassLoader classLoader) {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.setResourceLoader(new DefaultResourceLoader(classLoader));
            scanner.addIncludeFilter(new AssignableTypeFilter(Record.class));
            return scanner.findCandidateComponents(basePackage).stream()
                    .<Class<?>>map(candidate -> ClassUtils.resolveClassName(
                            requireNonNull(candidate.getBeanClassName()), classLoader))
                    .toList();
        }

        private static void register(RuntimeHints hints, BeanDescriptor bean) {
            rejectUnsupported(bean);

            hints.reflection().registerType(bean.getElementClass(), ACCESS_DECLARED_FIELDS);

            constraintsOf(bean)
                    .flatMap(constraint -> constraint.getConstraintValidatorClasses().stream())
                    .forEach(validator ->
                            hints.reflection().registerType(validator, INVOKE_DECLARED_CONSTRUCTORS));
        }

        private static void rejectUnsupported(BeanDescriptor bean) {
            for (PropertyDescriptor property : bean.getConstrainedProperties()) {
                if (property.isCascaded() || !property.getConstrainedContainerElementTypes().isEmpty()) {
                    throw new IllegalStateException(bean.getElementClass().getName() + "."
                            + property.getPropertyName()
                            + ": cascaded validation and container element constraints are not supported");
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
