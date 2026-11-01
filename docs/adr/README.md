# ADR (아키텍처 결정 기록)

구조나 일하는 방식을 정할 때, 무엇을 왜 골랐고 무엇을 고르지 않았는지를 파일 하나에 남깁니다. 지금 코드가 지키는 규칙을 한눈에 보려면 저장소 [README](../../README.md) 의 "봇 서버 구조" 와 `ArchitectureTest` 를, 그 규칙이 왜 그런지는 여기 ADR 을 봅니다.

## 쓰는 법

- 파일 이름은 `NNNN-english-slug.md` 입니다. 번호는 네 자리로 차례대로 붙이고, 뒤에는 결정을 줄인 영어 소문자 낱말을 `-` 로 잇습니다. 다른 PR 이 이미 고른 번호는 건너뛰고, 두 PR 이 같은 번호를 고르면 나중에 머지되는 쪽이 번호를 올립니다.
- 본문은 한국어 합니다체로 씁니다. 제목(`# ...`)은 결정 한 문장입니다.
- 제목 바로 아래에 두 줄을 둡니다.

  ```markdown
  - 상태: 채택
  - 날짜: 2026-10-18
  ```

  - 상태는 셋 중 하나입니다.
    - `채택`: 본문의 결정이 모두 지금도 맞습니다.
    - `일부 대체됨`: 결정의 일부를 뒤의 ADR 이 바꿨습니다. 나머지는 지금도 맞습니다.
    - `대체됨`: 결정 전체를 뒤의 ADR 이 바꿨습니다.
  - `일부 대체됨` 과 `대체됨` 은 상태 줄 아래에 바뀐 것을 한 줄씩 적습니다. 무엇이 어떻게 바뀌었는지와 바꾼 ADR 을 적어서, 상태 줄만 읽어도 본문의 어디가 지금과 다른지 알 수 있게 합니다.

    ```markdown
    - 상태: 일부 대체됨
      - 사용 중 응답은 423 대신 409 + 오류 코드 `voice-channel-in-use` → ADR 0013
    - 날짜: 2026-10-11
    ```

  - 날짜는 그 파일이 저장소에 처음 들어온 날입니다(`git log --diff-filter=A --follow --format=%ad --date=short -- <파일> | tail -1`). 0001~0017 은 저장소 첫 커밋 날(2026-10-11)이라, 본문에 적힌 날(예: 2026-10-09)처럼 실제로 정한 날은 그보다 이를 수 있습니다.
- 첫 문단에는 왜 정해야 했는지(그때의 문제)를 쓰고, 그 아래를 이렇게 나눕니다.
  - `## 결정`: 무엇을 하기로 했나
  - `## 결과`: 그래서 무엇이 바뀌었고, 무엇을 받아들였나
  - `## 고르지 않은 것`: 함께 놓고 본 다른 방법과 고르지 않은 이유

  0001~0013 은 `## 결정` 없이 `## 결과` 에 결정까지 함께 적었습니다. 그대로 둡니다.

## 결정이 바뀔 때

이제부터(ADR 0034) 채택된 ADR 의 본문은 고치지 않습니다.

1. 바뀐 결정은 새 ADR 로 씁니다. 새 ADR 의 첫 문단에 어느 ADR 의 무엇을 바꾸는지 적습니다.
2. 옛 ADR 은 상태 줄만 `일부 대체됨` 이나 `대체됨` 으로 바꾸고 그 아래에 바뀐 것을 한 줄 더합니다(`  - 무엇이 어떻게 → ADR 새번호`). 아래 목록의 상태도 함께 고칩니다.
3. 저장소 README 와 `ArchitectureTest` 처럼 지금 규칙을 설명하는 곳은 새 결정에 맞게 고칩니다.

오타, 깨진 링크처럼 뜻이 바뀌지 않는 고침은 본문을 고쳐도 됩니다.

0001~0033 에는 그 전 방식대로 본문을 바로 고친 흔적(`~~취소선~~`, "ADR 00xx 로 바뀌었습니다")이 남아 있습니다. 결정이 어떻게 바뀌어 왔는지 보여 주는 기록이라 지우지 않습니다.

## 목록

### 컨텍스트와 계층

| ADR | 제목 | 상태 |
| --- | --- | --- |
| [0001](0001-hexagonal-bot-server.md) | 봇 서버는 헥사고날 구조로 짓고 ArchUnit 으로 지킨다 | 일부 대체됨 (ADR 0009, ADR 0045) |
| [0002](0002-bounded-contexts.md) | 봇 서버를 speech, device, shared 컨텍스트로 나눈다 | 일부 대체됨 (ADR 0015, ADR 0025, ADR 0031, ADR 0047, ADR 0048) |
| [0012](0012-application-subpackages.md) | application 의 유스케이스, Command, Dto, 예외를 폴더로 나눈다 | 일부 대체됨 (ADR 0024, ADR 0029, ADR 0032, ADR 0041) |
| [0023](0023-properties-per-adapter.md) | 설정 값은 그 값을 쓰는 어댑터 패키지에 둔다 | 일부 대체됨 (ADR 0047) |
| [0026](0026-calls-between-contexts.md) | 컨텍스트 사이 호출은 컨텍스트 지도와 공개 입구로만 한다 | 일부 대체됨 (ADR 0031, ADR 0033, ADR 0040) |
| [0032](0032-archunit-checks-dependencies-only.md) | ArchUnit 은 의존 방향만 검사한다 | 일부 대체됨 (ADR 0035) |
| [0035](0035-archunit-patterns-and-domain-allowlist.md) | 도메인과 애플리케이션이 어댑터를 모르는지는 폴더 이름과 상관없이 검사하고, 도메인은 써도 되는 것만 적는다 | 채택 |
| [0040](0040-inbound-adapters-do-not-call-other-contexts.md) | 인바운드 어댑터는 다른 컨텍스트의 유스케이스를 부르지 않고, 다른 컨텍스트 소식은 공개 이벤트로 받는다 | 채택 |
| [0041](0041-use-cases-directly-in-port-in.md) | 입력 포트(...UseCase)는 port/in 바로 아래에 둔다 | 채택 |
| [0045](0045-apps-libs-build-logic-layout.md) | 저장소를 apps, libs, build-logic 으로 나눈다 | 채택 |
| [0047](0047-split-settings-and-synthesis-from-speech.md) | speech 에서 사용자 설정(settings)과 음성 만들기(synthesis)를 떼어 낸다 | 채택 |
| [0048](0048-rename-device-context-to-identity.md) | device 컨텍스트를 identity 로 부른다 | 채택 |

### 도메인 모델

| ADR | 제목 | 상태 |
| --- | --- | --- |
| [0003](0003-value-objects-and-aggregates.md) | 값 객체는 domain/vo 의 record, 애그리거트는 식별자로 비교하는 class | 일부 대체됨 (ADR 0014) |
| [0024](0024-rules-live-in-the-domain.md) | 데이터 상태로 정하는 규칙은 애그리거트와 값 객체에 둔다 | 일부 대체됨 (ADR 0032) |
| [0052](0052-domain-model-tidy.md) | 도메인 모델을 작게 바로잡는다: 채널 ID, 화면용 채널 정보, 되살리기, 음성 배열, 허용 목록 확인 | 채택 |

### 이름

| ADR | 제목 | 상태 |
| --- | --- | --- |
| [0004](0004-output-port-naming.md) | 출력 포트 이름은 Repository 대신 ...Port 로 끝낸다 | 일부 대체됨 (ADR 0032, ADR 0039, ADR 0041) |
| [0027](0027-naming-rules.md) | 이름 규칙을 한곳에 모은다 | 일부 대체됨 (ADR 0032, ADR 0039, ADR 0044) |
| [0039](0039-outbound-adapters-end-with-adapter.md) | 출력 포트를 구현한 어댑터는 이름을 Adapter 로 끝낸다 | 채택 |
| [0044](0044-discord-vocabulary-first-names.md) | 이름은 디스코드 말을 먼저, 없으면 실무 표준을 쓴다 | 채택 |

### 서비스와 유스케이스 입출력

| ADR | 제목 | 상태 |
| --- | --- | --- |
| [0008](0008-one-service-per-use-case.md) | 서비스는 유스케이스(입력 포트) 하나만 구현한다 | 일부 대체됨 (ADR 0014, ADR 0032, ADR 0042) |
| [0009](0009-spring-annotations-on-services.md) | 서비스에는 @Service, @RequiredArgsConstructor 를 붙이고 @Transactional 은 필요한 곳에만 쓴다 | 일부 대체됨 (ADR 0011, ADR 0028, ADR 0033) |
| [0010](0010-use-cases-return-dtos.md) | 유스케이스는 애그리거트 대신 Dto 를 돌려준다 | 일부 대체됨 (ADR 0030, ADR 0032, ADR 0041, ADR 0052) |
| [0011](0011-lombok-constructors-for-beans.md) | 필드 주입만 하는 스프링 빈은 모두 @RequiredArgsConstructor 를 쓴다 | 채택 |
| [0029](0029-commands-and-queries.md) | 유스케이스는 Command 나 Query 하나를 받는다 | 일부 대체됨 (ADR 0031, ADR 0032, ADR 0042) |
| [0042](0042-one-method-per-use-case.md) | 유스케이스(입력 포트) 하나에는 메서드 하나만 둔다 | 채택 |

### 예외

| ADR | 제목 | 상태 |
| --- | --- | --- |
| [0025](0025-business-exceptions-by-kind.md) | 예상한 실패는 공통 부모와 종류로 나누고, 어댑터마다 한 곳에서 처리한다 | 일부 대체됨 (ADR 0032, ADR 0043, ADR 0044) |

### 데이터와 동시성

| ADR | 제목 | 상태 |
| --- | --- | --- |
| [0016](0016-mysql-and-flyway.md) | 배포 서버는 MySQL, 스키마는 Flyway 로 관리한다 | 일부 대체됨 (ADR 0038) |
| [0028](0028-one-aggregate-per-write.md) | 한 번에 함께 저장하는 것은 애그리거트 하나다 | 일부 대체됨 (ADR 0033, ADR 0049) |
| [0033](0033-transactional-on-writing-use-cases.md) | DB 에 쓰는 유스케이스에는 @Transactional 을 붙인다 | 일부 대체됨 (ADR 0049) |
| [0038](0038-jpa-entities-with-persistence-mappers.md) | 저장소 어댑터는 JPA 엔티티를 도메인과 따로 두고 매퍼로 옮긴다 | 일부 대체됨 (ADR 0039) |
| [0049](0049-store-pairings-in-database.md) | 연결 코드도 DB 에 두고, 기기 테이블의 기본 키는 기기 ID 로 한다 | 채택 |

### 웹 API

| ADR | 제목 | 상태 |
| --- | --- | --- |
| [0005](0005-device-token-pairing.md) | PC 는 /연결 코드로 받은 기기 토큰으로 인증한다 | 일부 대체됨 (ADR 0031) |
| [0013](0013-api-versioning-and-error-codes.md) | API 경로에 버전을 붙이고, 오류는 코드로 구분한다 | 일부 대체됨 (ADR 0025) |
| [0017](0017-api-integration-test.md) | API 통합 테스트는 디스코드와 TTS 엔진만 출력 포트 대역으로 바꾼다 | 일부 대체됨 (ADR 0046) |
| [0030](0030-web-mappers.md) | 웹 요청·응답 변환은 컨트롤러 옆 웹 매퍼가 맡는다 | 일부 대체됨 (ADR 0031, ADR 0032, ADR 0038) |
| [0031](0031-device-token-authentication-with-spring-security.md) | 기기 토큰 인증은 스프링 시큐리티 필터가 맡는다 | 일부 대체됨 (ADR 0036) |

### 디스코드와 음성

| ADR | 제목 | 상태 |
| --- | --- | --- |
| [0006](0006-free-tts-engines.md) | TTS 는 무료 비공식 엔진을 쓰고, 실패하면 Google 번역 목소리로 대신 읽는다 | 일부 대체됨 (ADR 0047) |
| [0007](0007-first-come-voice-channel.md) | 같은 디스코드 서버에서는 먼저 쓰고 있는 음성 채널이 우선이다 | 일부 대체됨 (ADR 0013, ADR 0014) |
| [0014](0014-bot-seat-aggregate.md) | 디스코드 서버별 봇 자리(BotSeat)를 speech 의 애그리거트로 둔다 | 일부 대체됨 (ADR 0043, ADR 0044) |
| [0015](0015-guild-allowlist.md) | 봇이 머물 디스코드 서버를 허용 목록으로 정한다 (guild 컨텍스트) | 채택 |
| [0019](0019-spoken-text-cleanup.md) | 문장은 speech 도메인에서 읽기 좋게 다듬은 뒤 엔진에 보낸다 | 일부 대체됨 (ADR 0051) |
| [0051](0051-text-normalizer.md) | 문장 다듬기 규칙은 speech/domain/normalization 의 TextNormalizer 에 둔다 | 채택 |
| [0043](0043-play-speech-from-memory.md) | 음성은 임시 파일 없이 메모리에서 바로 재생한다 | 채택 |
| [0046](0046-discord-shard-manager.md) | 디스코드 연결은 처음부터 ShardManager 로 만든다 | 채택 |

### 운영

| ADR | 제목 | 상태 |
| --- | --- | --- |
| [0036](0036-health-and-metrics-with-actuator.md) | 상태 확인과 지표는 Actuator 로 보고, 디스코드 연결과 TTS 를 거기에 더한다 | 일부 대체됨 (ADR 0046) |
| [0053](0053-quick-chat-speed-metrics.md) | 핵심 약속 "빠름" 을 quick chat 지표 두 개로 잰다 | 채택 |
| [0037](0037-cache-tts-results.md) | 엔진이 만든 음성은 스프링 캐시(Caffeine)로 기억한다 | 채택 |

### 데스크톱 클라이언트와 배포

| ADR | 제목 | 상태 |
| --- | --- | --- |
| [0018](0018-windows-installer.md) | Windows 에는 jpackage 설치 마법사(.exe)로 설치한다 | 일부 대체됨 (ADR 0020, ADR 0021) |
| [0021](0021-installer-look.md) | 설치 마법사는 앱 색의 그림과 한국어 문구로 꾸민다 | 채택 |
| [0050](0050-desktop-input-ui-tests.md) | 데스크톱 입력창은 TestFX 로 화면 없이 띄워 테스트한다 | 채택 |

### 릴리스

| ADR | 제목 | 상태 |
| --- | --- | --- |
| [0020](0020-github-releases-and-update-notice.md) | 설치 파일은 GitHub Releases 로 나눠 주고, 새 버전은 트레이 알림으로 알린다 | 일부 대체됨 (ADR 0022) |
| [0022](0022-release-notes-by-pr-type.md) | Release 노트는 PR 제목의 타입으로 나눈다 | 채택 |

### ADR 쓰는 법

| ADR | 제목 | 상태 |
| --- | --- | --- |
| [0034](0034-adr-status-and-index.md) | ADR 에 상태와 날짜를 붙이고, 바뀐 결정은 새 ADR 로 쓴다 | 채택 |
