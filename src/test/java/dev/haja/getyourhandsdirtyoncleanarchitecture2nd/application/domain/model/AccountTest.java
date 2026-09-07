package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    @Nested
    class AccountId_생성 {

        @Test
        void value를_그대로_보관한다() {
            AccountId id = new AccountId(1L);

            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        void 같은_value면_동등하다() {
            assertThat(new AccountId(1L)).isEqualTo(new AccountId(1L));
        }

        @Test
        void value가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new AccountId(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("value");
        }
    }
}
