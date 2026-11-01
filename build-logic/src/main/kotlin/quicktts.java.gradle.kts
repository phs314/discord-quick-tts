// 모든 자바 모듈의 공통 설정: JDK 25, UTF-8, JUnit 5.
plugins {
    java
}

group = "io.github.phs314.quicktts"
// version 은 gradle.properties 에서 정하고, 태그 빌드는 -Pversion=1.2.3 으로 덮어쓴다.

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
