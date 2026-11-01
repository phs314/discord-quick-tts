plugins {
    id("quicktts.spring-app")
}

dependencies {
    implementation(project(":libs:api-contract"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    // 기기 토큰 인증. 토큰으로 사람을 알아내는 필터만 우리가 쓰고 나머지는 스프링 시큐리티 기본을 따른다.
    implementation("org.springframework.boot:spring-boot-starter-security")
    // 저장소 어댑터는 JPA 엔티티를 도메인과 따로 두고 매퍼로 옮긴다 (ADR 0038).
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // 설정 값(@ConfigurationProperties)의 필수 값을 켜질 때 검사한다.
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // 스키마는 Flyway 마이그레이션(resources/db/migration)으로 관리한다.
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    // 상태 확인(/actuator/health)과 지표(Micrometer).
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // 같은 문장의 음성을 기억해 두는 캐시.
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.flywaydb:flyway-mysql")
    // 웹 API 문서(OpenAPI). 컨트롤러에서 /v3/api-docs 를 만들고 /swagger-ui.html 로 보여 준다.
    implementation(libs.springdoc.webmvc.ui)
    // 배포 서버는 MySQL, 이 PC 에서 띄우는 서버(개발, exe)는 설치가 필요 없는 H2 파일 DB.
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.h2database:h2")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation(libs.jda)
    implementation(libs.lavaplayer)
    // 디스코드 음성 연결에 필요한 DAVE(종단간 암호화) 구현
    implementation(libs.jdave.api)
    runtimeOnly(libs.jdave.natives.windows)
    runtimeOnly(libs.jdave.natives.linux)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // 컨트롤러 테스트(@WebMvcTest)가 MVC, JSON, 보안 필터를 실제 설정 그대로 올린다.
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation(libs.archunit.junit5)
    // 저장소 테스트를 진짜 MySQL 에서도 돌린다. Docker 가 있어야 한다.
    testImplementation("org.testcontainers:testcontainers-mysql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.bootRun {
    // 저장소 루트의 .env 를 읽을 수 있도록 루트에서 실행한다.
    workingDir = rootProject.projectDir
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
