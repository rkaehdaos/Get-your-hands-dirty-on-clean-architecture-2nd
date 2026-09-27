plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.hibernate.orm") version "7.4.5.Final"
    id("org.graalvm.buildtools.native") version "1.1.8"
}

group = "dev.haja"
version = "0.0.1-SNAPSHOT"
description = "Get-your-hands-dirty-on-clean-architecture-2nd"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("jakarta.transaction:jakarta.transaction-api")

    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-resttestclient")

    // bootRun(developmentOnly를 상속한다)과 테스트는 H2를 얻지만 bootJar와
    // productionRuntimeClasspath에는 실리지 않는다. runtimeOnly로 두면 프로덕션에서
    // datasource 설정이 빠졌을 때 기동 실패 대신 빈 인메모리 DB로 조용히 떠 버린다.
    testAndDevelopmentOnly("com.h2database:h2")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

hibernate { enhancement {} }

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

// 리포트는 actual 커버리지다 — classDirectories에서 아무것도 빼지 않는다.
tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

// Cleaned Code Coverage 100%: 테스트로 덮여야 하는 코드는 하나도 빠짐없이 실행돼야 한다.
// 제외는 여기(검증 규칙)에만 두고 리포트는 actual로 남긴다 — 둘의 차이가 곧 제외된 양이다.
tasks.jacocoTestCoverageVerification {
    // 실행 데이터가 없으면 검증 태스크는 실패하지 않고 조용히 SKIP된다.
    // executionData(test)는 mustRunAfter만 걸므로 단독 실행에서도 test가 먼저 돌게 한다.
    dependsOn(tasks.test)

    violationRules {
        // LINE은 부분 커버 라인을 커버로 센다. INSTRUCTION 누락 0이면 누락 라인과 부분 커버
        // 라인이 모두 0이고, BRANCH 누락 0이 한 번도 선택되지 않은 분기까지 잡는다.
        // element = CLASS라 실패 메시지에 클래스 이름이 찍힌다.
        rule {
            element = "CLASS"
            // Hibernate bytecode enhancement가 엔티티에 주입한 $$_hibernate_* 메서드는
            // 라인 정보가 없어 INSTRUCTION·BRANCH만 오염시킨다. 엔티티는 아래 규칙이 맡는다.
            excludes = listOf("*JpaEntity")
            limit {
                counter = "INSTRUCTION"
                value = "MISSEDCOUNT"
                maximum = BigDecimal.ZERO
            }
            limit {
                counter = "BRANCH"
                value = "MISSEDCOUNT"
                maximum = BigDecimal.ZERO
            }
        }
        // 엔티티는 통째로 빼지 않는다. 주입 코드가 없는 LINE으로 손으로 쓴 코드를 검사한다.
        rule {
            element = "CLASS"
            includes = listOf("*JpaEntity")
            limit {
                counter = "LINE"
                value = "MISSEDCOUNT"
                maximum = BigDecimal.ZERO
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
