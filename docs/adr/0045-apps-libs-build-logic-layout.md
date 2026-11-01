# 저장소를 apps, libs, build-logic 으로 나눈다

- 상태: 채택
- 날짜: 2026-11-01

모듈 셋(`bot-server`, `desktop-client`, `common`)이 저장소 맨 위에 나란히 있었고, 모든 모듈의 공통 설정은 루트 `build.gradle.kts` 의 `subprojects {}` 한 곳에 있었습니다. 지금은 문제가 없지만, 앱이 늘면(웹 화면, 서버를 나눈 두 번째 프로세스) 두 가지가 커집니다.

- 맨 위에서 무엇이 띄우는 프로그램이고 무엇이 나눠 쓰는 코드인지 이름으로만 알아야 합니다. `common` 은 이름만으로는 안에 무엇이 있는지 모릅니다.
- `subprojects {}` 는 모든 모듈에 같은 설정을 밀어 넣습니다. 자바 모듈이 아닌 모듈(웹 화면)이 생기면 예외를 계속 달아야 하고, Gradle 도 공유 설정은 `build-logic` 의 규칙 플러그인으로 두라고 권합니다.

## 결정

1. **맨 위는 셋입니다.**
   - `apps/`: 띄우는 프로그램. `apps/bot-server`, `apps/desktop-client`. 웹 화면이 생기면 `apps/web` 입니다.
   - `libs/`: 앱끼리 나눠 쓰는 코드. 업무 규칙은 두지 않고 앱 사이의 약속만 둡니다. 지금은 `libs/api-contract`(← `common`) 하나입니다.
   - `build-logic/`: 모든 모듈이 같이 쓰는 빌드 설정. Gradle 결과물 폴더 `build/` 와 헷갈리지 않게 `build-logic` 으로 부릅니다.
2. **Gradle 프로젝트 경로는 폴더와 같습니다**: `:apps:bot-server`, `:apps:desktop-client`, `:libs:api-contract`. 실행은 `./gradlew :apps:bot-server:bootRun` 입니다.
3. **공통 설정은 규칙 플러그인 둘**입니다. `quicktts.java` 는 JDK 25, UTF-8, JUnit 5, 그룹, 저장소를 정하고, `quicktts.spring-app` 은 `quicktts.java` 에 스프링 부트와 의존성 관리를 더합니다. 버전은 `gradle/libs.versions.toml` 하나에서 정하고, `build-logic` 도 같은 파일을 읽습니다. 루트의 `subprojects {}` 는 없앱니다.
4. **`common` 은 `libs/api-contract` 로, 패키지는 `io.github.phs314.quicktts.api` 로** 바꿉니다. 안에 든 것(웹 경로, 요청·응답 형식, 오류 코드)이 이름에 보이게 하려는 것입니다.
5. 바운디드 컨텍스트는 지금처럼 `apps/bot-server` 안의 패키지입니다. 컨텍스트마다 Gradle 모듈을 따로 두지 않습니다.

## 결과

- 소스 경로가 `apps/bot-server/src/...`, `apps/desktop-client/src/...`, `libs/api-contract/src/...` 로 바뀌었습니다. 설치 파일 워크플로의 경로와 태스크 이름(`:apps:desktop-client:packageInstaller`)도 바꿨고, `build-logic/**` 가 바뀌어도 설치 파일 확인이 돕니다.
- 코드와 동작은 바뀌지 않았습니다. 패키지 이름만 `quicktts.common` 에서 `quicktts.api` 로 바뀌었습니다.
- IDE 가 옛 경로를 기억하고 있으면 Java 언어 서버 작업 공간을 한 번 지우고 다시 엽니다.

## 고르지 않은 것

- `contexts/` 폴더에 컨텍스트마다 Gradle 모듈 두기: 의존 방향은 ArchUnit 이 이미 지키고, 팀·배포가 하나라 모듈을 나눠 얻는 것보다 빌드 설정과 모듈 사이 공개 범위 관리가 더 큽니다. 컨텍스트를 따로 배포하게 되면 그때 모듈로 뺍니다.
- 규칙 플러그인을 모듈 종류마다 잘게(`quicktts.library`, `quicktts.javafx-app` 등): 지금 모듈 셋에 둘이면 충분합니다.
- 프로젝트 경로를 짧게(`:bot-server`) 두고 폴더만 옮기기: 명령은 그대로지만 폴더와 경로가 달라서 새 모듈을 찾을 때 헷갈립니다.
