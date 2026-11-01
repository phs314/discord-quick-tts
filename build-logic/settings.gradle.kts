// 모든 모듈이 같이 쓰는 빌드 설정(규칙 플러그인). 버전은 저장소의 gradle/libs.versions.toml 하나에서 읽는다.
dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
