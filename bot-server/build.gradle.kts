plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    // 스키마는 Flyway 마이그레이션(resources/db/migration)으로 관리한다.
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-mysql")
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
