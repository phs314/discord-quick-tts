# 봇 서버를 speech, device, shared 컨텍스트로 나눈다

"문장을 음성 채널에서 읽어 주기" 와 "이 PC 가 누구 것인지 알기" 는 쓰는 말도 바뀌는 이유도 다릅니다. 계층으로만 나누면 이 경계가 코드에 드러나지 않아서, 패키지를 먼저 바운디드 컨텍스트로 나누고 각 컨텍스트 안에서 헥사고날 계층([0001](0001-hexagonal-bot-server.md))을 지키기로 했습니다.

- `speech` (핵심): 누가 무슨 문장을 어느 음성 채널에서 어떤 목소리로 읽게 하나.
- `device` (지원): `/연결` 코드 발급, 기기 등록, 기기 인증, 연결 해제.
- `shared` (공유 커널): 두 컨텍스트가 함께 쓰는 `DiscordUserId` 와 도메인 검증 예외, 공통 예외 처리.

컨텍스트 사이 규칙은 `ArchitectureTest` 가 확인합니다.

- `speech` 는 `device` 를 공개 입구인 `device.application.port.in` 으로만 씁니다. 실제 접점은 quick chat 컨트롤러가 기기 토큰의 주인을 묻는 `AuthenticateDeviceUseCase` 하나입니다.
- `device` 는 `speech` 를 모르고, `shared` 는 어느 컨텍스트도 모릅니다.

## 결과

- `AuthenticateDeviceUseCase` 는 `DeviceToken` 대신 토큰 원문 `String` 을 받습니다. `speech` 가 device 도메인 타입을 알지 않게 하려는 것입니다.
- `shared` 에는 정말 둘 다 쓰는 것만 둡니다. 한쪽만 쓰는 값을 편하다고 `shared` 로 옮기지 않습니다.
