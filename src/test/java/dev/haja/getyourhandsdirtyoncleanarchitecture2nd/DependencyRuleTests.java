package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

// ArchUnit은 클래스 파일을 런타임에 스캔하는데 네이티브 이미지 안에는 스캔할 클래스 파일이 없다.
// 정적 구조 검사라 JVM 테스트로 충분하므로 네이티브에서 대신 덮는 테스트는 없다.
@DisabledInNativeImage
class DependencyRuleTests {

    public static final String ROOT_PACKAGE_NAME = "dev.haja.getyourhandsdirtyoncleanarchitecture2nd";

    @Test
    void domainModelDoesNotDependOnOutside() {
        noClasses()
            .that()
            .resideInAPackage(ROOT_PACKAGE_NAME + ".application.domain.model..")
            .should()
            .dependOnClassesThat()
            .resideOutsideOfPackages(
                ROOT_PACKAGE_NAME + ".application.domain.model..",
                "lombok..",
                "java.."
            )
            // 테스트 클래스패스에는 같은 패키지의 테스트(AccountTest 등)도 있다. 이들은 AssertJ·JUnit과
            // 테스트 데이터 빌더에 의존하므로, 빼지 않으면 프로덕션 코드가 아닌 테스트가 위반으로 잡힌다.
            .check(new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(ROOT_PACKAGE_NAME + ".."));
    }
}
