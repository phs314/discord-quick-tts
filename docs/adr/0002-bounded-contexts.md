# 봇 서버를 speech, device, shared 컨텍스트로 나눈다

- 상태: 일부 대체됨
  - guild 컨텍스트가 더해지고 `GuildId` 는 shared 로 → ADR 0015
  - 공통 예외 처리는 shared 의 어댑터로 → ADR 0025
  - speech 가 device 를 부르던 길(기기 토큰 주인 묻기)이 없어짐 → ADR 0031
  - speech 에서 사용자 설정(settings)과 음성 만들기(synthesis)를 뗌 → ADR 0047
  - `device` 컨텍스트 이름을 `identity` 로 → ADR 0048
- 날짜: 2026-10-11

"문장을 음성 채널에서 읽어 주기" 와 "이 PC 가 누구 것인지 알기" 는 쓰는 말도 바뀌는 이유도 다릅니다. 계층으로만 나누면 이 경계가 코드에 드러나지 않아서, 패키지를 먼저 바운디드 컨텍스트로 나누고 각 컨텍스트 안에서 헥사고날 계층([0001](0001-hexagonal-bot-server.md))을 지키기로 했습니다.

- `speech` (핵심): 누가 무슨 문장을 어느 음성 채널에서 어떤 목소리로 읽게 하나.
- `device` (지원): `/연결` 코드 발급, 기기 등록, 기기 인증, 연결 해제.
- ~~`shared` (공유 커널): 두 컨텍스트가 함께 쓰는 `DiscordUserId` 와 도메인 검증 예외, 공통 예외 처리.~~
- `shared` (공유 커널): 둘 이상의 컨텍스트가 함께 쓰는 것. 도메인에는 `DiscordUserId`, `GuildId`(ADR 0015 로 speech 에서 옮김)와 도메인 검증 예외, 어댑터에는 공통 예외 처리(ADR 0025 로 `ApiExceptionHandler`, `FailureReplyingListener`, 그리고 `ApiProblem`), 슬래시 명령 묶음(`DiscordSlashCommand`), 어느 컨텍스트의 규칙도 쓰지 않는 `/도움말` 안내가 있습니다.
- `guild` (지원): 봇이 머물 디스코드 서버 허용 목록. ADR 0015 로 더해졌고, `speech`, `device` 와 서로 모릅니다.

컨텍스트 사이 규칙은 `ArchitectureTest` 가 확인합니다.

- `speech` 는 `device` 를 공개 입구인 `device.application.port.in` 으로만 씁니다. 실제 접점은 quick chat 컨트롤러가 기기 토큰의 주인을 묻는 `AuthenticateDeviceUseCase` 하나였는데, ADR 0031 에서 기기 토큰 인증을 device 의 스프링 시큐리티 필터로 옮기며 없어졌습니다.
- `device` 는 `speech` 를 모르고, `guild` 와 다른 두 컨텍스트는 서로 모르며, `shared` 는 어느 컨텍스트도 모릅니다.

## 결과

- `AuthenticateDeviceUseCase` 는 `DeviceToken` 대신 토큰 원문 `String` 을 받습니다. `speech` 가 device 도메인 타입을 알지 않게 하려는 것입니다.
- `shared` 에는 정말 둘 다 쓰는 것만 둡니다. 한쪽만 쓰는 값을 편하다고 `shared` 로 옮기지 않습니다.
