package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.context.properties.bind.validation.ValidationBindHandler;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

// BuckPalConfigurationPropertiesTest는 네이티브 이미지에서 돌지 않는다. 이 테스트는 그 빈자리를
// 채운다 — 기동 시 바인딩과 같은 경로(Binder + ValidationBindHandler)로 record를 만들고
// 검증해, 네이티브에서도 record 필드의 제약을 읽을 수 있는지 확인한다. 그 리플렉션 힌트는 AOT가
// 이 컨텍스트의 설정 프로퍼티 빈을 보고 등록한다. 컨텍스트 자체는 application.yml로 정상 기동한다.
@SpringBootTest
class BuckPalConfigurationPropertiesValidationTest {

    @Autowired
    private Validator validator;

    @Test
    @DisplayName("송금 한도 설정이 없으면 바인딩이 검증에서 실패함")
    void givenNoTransferThreshold_thenBindingFailsValidation() {

        // given
        Binder binder = binderOf(Map.of());

        // when / then
        assertThatThrownBy(() -> bind(binder))
                .isInstanceOf(BindException.class)
                .hasRootCauseInstanceOf(BindValidationException.class)
                .rootCause()
                .hasMessageContaining("transferThreshold");
    }

    @Test
    @DisplayName("송금 한도가 0이면 바인딩이 검증에서 실패함")
    void givenZeroTransferThreshold_thenBindingFailsValidation() {

        // given
        Binder binder = binderOf(Map.of("buckpal.transferThreshold", "0"));

        // when / then
        assertThatThrownBy(() -> bind(binder))
                .isInstanceOf(BindException.class)
                .hasRootCauseInstanceOf(BindValidationException.class)
                .rootCause()
                .hasMessageContaining("transferThreshold");
    }

    private static Binder binderOf(Map<String, String> properties) {
        return new Binder(new MapConfigurationPropertySource(properties));
    }

    // 생성자 바인딩 대상은 값이 하나도 없어도 만들어져 검증되므로 bind가 아니라 bindOrCreate다
    private void bind(Binder binder) {
        binder.bindOrCreate("buckpal", Bindable.of(BuckPalConfigurationProperties.class),
                new ValidationBindHandler(new SpringValidatorAdapter(validator)));
    }
}
