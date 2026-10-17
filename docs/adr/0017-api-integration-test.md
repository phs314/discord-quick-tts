# API 통합 테스트는 디스코드와 TTS 엔진만 출력 포트 대역으로 바꾼다

지금까지 API 쪽 테스트는 컨트롤러 하나에 가짜 유스케이스를 끼운 MockMvc 테스트(`QuickChatControllerTest`, `DeviceControllerTest`)뿐이었습니다. 그래서 빈 연결, 설정, Flyway 마이그레이션, JSON 변환, 예외 처리기가 실제 서버에서 함께 맞물리는지는 exe 를 켜서 디스코드로 확인해야 알 수 있었습니다. 배포하면 클라이언트와 봇 서버가 따로 바뀌므로(ADR 0013), `/api/v1` 약속이 깨졌는지 빌드에서 알아야 합니다.

## 결정

- `QuickTtsApiIntegrationTest` 가 `@SpringBootTest(webEnvironment = RANDOM_PORT)` 로 봇 서버를 통째로 띄우고, 데스크톱 클라이언트처럼 HTTP 로 `/api/v1` 을 부릅니다. 요청·응답 본문은 클라이언트와 같은 common DTO 를 씁니다.
- 컨트롤러, 서비스, 도메인, 저장소, Flyway 는 실제 것을 씁니다. 바꾸는 것은 바깥 세계에 닿는 출력 포트뿐입니다.
  - 디스코드: `VoiceChannelLocatorPort`, `SpeechPlayerPort` 는 서비스 테스트와 같은 대역(`FakeVoiceChannelLocatorPort`, `FakeSpeechPlayerPort`)으로 바꿉니다. 소리는 나지 않고, 봇이 들어간 채널과 읽은 문장을 글로 남깁니다.
  - TTS 엔진: `SpeechSynthesizerPort` 는 `FakeSpeechSynthesizerPort` 로 바꿉니다. Edge, Google 번역을 부르지 않습니다.
  - JDA 는 Mockito 빈 껍데기로 바꿔 디스코드에 로그인하지 않게 하고, 켜질 때 디스코드를 부르는 `DiscordCommandRegistrar`, `BotOwnerPort`, `JoinedGuildPort` 도 함께 바꿉니다.
- 디스코드 `/연결`, `/연결해제`, `/목소리` 로 할 일은 그 명령이 부르는 입력 포트를 테스트가 직접 부릅니다.
- DB 는 PC 서버와 같은 H2(MySQL 모드)를 메모리로 띄웁니다. MySQL 에서 SQL 이 도는지는 저장소 테스트가 이미 양쪽에서 확인합니다(ADR 0016).
- 컨트롤러 MockMvc 테스트는 그대로 둡니다. 오류 코드 하나하나는 거기서 빠르게 보고, 통합 테스트는 실제 서버에서 요청부터 응답까지 이어지는 흐름을 봅니다.

## 고르지 않은 것

- 바깥 HTTP(Edge, Google 번역)를 WireMock 같은 가짜 서버로 받기: 엔진 어댑터는 이미 따로 테스트하고, 비공식 API 의 응답 모양을 테스트에 박제하면 엔진이 바뀔 때마다 고쳐야 합니다.
- JDA 를 깊게 흉내 내 디스코드 쪽 어댑터까지 돌리기: JDA 내부 구조를 테스트가 알아야 해서 깨지기 쉽습니다. 디스코드 연동은 지금처럼 실제 디스코드에서 확인합니다.
- 통합 테스트도 MySQL 에서 돌리기: Spring 문맥을 두 번 띄워 빌드가 느려지는 데 비해, 저장소 테스트 이상으로 잡을 것이 거의 없습니다.

## 결과

- 새 API 나 오류 코드를 더하면 이 테스트에도 그 흐름을 하나 더합니다.
- 테스트끼리 봇 자리, 목소리 설정, 기기가 섞이지 않도록 사용자, 디스코드 서버, 채널마다 새 ID 를 씁니다. Spring 문맥은 한 번만 띄웁니다.
