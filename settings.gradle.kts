pluginManagement {
    // 모듈이 같이 쓰는 빌드 설정(quicktts.java, quicktts.spring-app)
    includeBuild("build-logic")
}

rootProject.name = "discord-quick-tts"

// apps: 띄우는 프로그램, libs: 앱끼리 나눠 쓰는 코드
include(
    ":apps:bot-server",
    ":apps:desktop-client",
    ":libs:api-contract",
)
