package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SpringDataAccountRepositoryTest {

    private static final long 없는_ID = 999L;

    @Autowired
    private SpringDataAccountRepository accountRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static AccountJpaEntity 신규계좌() {
        return new AccountJpaEntity(null);
    }

    private AccountJpaEntity 저장된계좌() {
        AccountJpaEntity saved = accountRepository.saveAndFlush(신규계좌());
        entityManager.clear();
        return saved;
    }

    private long 계좌_테이블_행수() {
        entityManager.flush();
        return ((Number) entityManager.getEntityManager()
                .createNativeQuery("select count(*) from account")
                .getSingleResult()).longValue();
    }

    @Nested
    class 저장 {

        @Test
        void 저장하면_ID가_생성된다() {
            AccountJpaEntity saved = accountRepository.save(신규계좌());

            assertThat(saved.getId()).isNotNull();
        }

        @Test
        void 저장할_때마다_다른_ID가_생성된다() {
            AccountJpaEntity first = accountRepository.save(신규계좌());
            AccountJpaEntity second = accountRepository.save(신규계좌());

            assertThat(first.getId()).isNotEqualTo(second.getId());
        }

        @Test
        void 저장한_계좌는_account_테이블의_행이_된다() {
            accountRepository.save(신규계좌());

            assertThat(계좌_테이블_행수()).isEqualTo(1L);
        }
    }

    @Nested
    class 조회 {

        @Test
        void 저장한_계좌를_ID로_조회한다() {
            AccountJpaEntity saved = 저장된계좌();

            Optional<AccountJpaEntity> found = accountRepository.findById(saved.getId());

            assertThat(found).contains(saved);
        }

        @Test
        void 없는_ID로_조회하면_빈_Optional을_반환한다() {
            Optional<AccountJpaEntity> found = accountRepository.findById(없는_ID);

            assertThat(found).isEmpty();
        }

        @Test
        void 저장한_계좌는_존재한다() {
            AccountJpaEntity saved = 저장된계좌();

            assertThat(accountRepository.existsById(saved.getId())).isTrue();
        }

        @Test
        void 없는_ID는_존재하지_않는다() {
            assertThat(accountRepository.existsById(없는_ID)).isFalse();
        }

        @Test
        void 저장한_계좌를_모두_조회한다() {
            AccountJpaEntity first = 저장된계좌();
            AccountJpaEntity second = 저장된계좌();

            assertThat(accountRepository.findAll())
                    .containsExactlyInAnyOrder(first, second);
        }

        @Test
        void 저장한_적이_없으면_개수는_0이다() {
            assertThat(accountRepository.count()).isZero();
        }
    }

    @Nested
    class 삭제 {

        @Test
        void 삭제한_계좌는_조회되지_않는다() {
            AccountJpaEntity saved = 저장된계좌();

            accountRepository.deleteById(saved.getId());

            assertThat(accountRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        void 삭제하면_account_테이블의_행이_사라진다() {
            AccountJpaEntity saved = 저장된계좌();

            accountRepository.deleteById(saved.getId());

            assertThat(계좌_테이블_행수()).isZero();
        }
    }
}
