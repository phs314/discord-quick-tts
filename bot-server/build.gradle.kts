plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("com.h2database:h2")

    implementation(libs.jda)
    implementation(libs.lavaplayer)
    // 디스코드 음성 연결에 필요한 DAVE(종단간 암호화) 구현
    implementation(libs.jdave.api)
    runtimeOnly(libs.jdave.natives.windows)
    runtimeOnly(libs.jdave.natives.linux)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.archunit.junit5)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.bootRun {
    // 저장소 루트의 .env 를 읽을 수 있도록 루트에서 실행한다.
    workingDir = rootProject.projectDir
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
