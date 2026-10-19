# 서비스는 유스케이스(입력 포트) 하나만 구현한다

- 상태: 일부 대체됨
  - 먼저 온 채널 규칙과 잠금은 `BotSeat`, `BotSeatPort` 로 → ADR 0014
  - 서비스 이름·구현 수 규칙은 리뷰로 지킴 → ADR 0032
  - 한 입력 포트 안의 여러 메서드를 한 서비스에 두던 것을 입력 포트마다 메서드 하나로 → ADR 0042
- 날짜: 2026-10-11

처음에는 서비스 하나가 비슷한 유스케이스 여러 개를 함께 구현했습니다. `DeviceRegistrationService` 는 연결 코드 발급, 기기 등록, 기기 인증, 내 기기 관리까지 입력 포트 4개를, `QuickChatService` 는 읽어 주기와 내 음성 채널 찾기 2개를 맡았습니다. 인바운드 어댑터는 입력 포트만 보므로 바깥에서는 차이가 없었지만, 안쪽에서는 두 가지가 불편했습니다.

- 이름이 하는 일과 어긋났습니다. "기기 등록" 서비스가 인증과 해제까지 했습니다.
- 의존이 부풀었습니다. 기기 인증은 `DevicePort` 하나면 되는데 `PairingPort` 와 `Clock` 까지 받았습니다.

세 가지를 놓고 골랐습니다.

- **입력 포트마다 서비스 하나** (고름): `XxxUseCase` 를 `XxxService` 하나가 구현합니다.
- 지금처럼 묶어 두고, 서비스 하나가 입력 포트 5개 이상이 되면 나누기: 당장 바꿀 것은 없지만 "언제 나누나" 를 매번 판단해야 합니다.
- 메서드마다 서비스 하나: 목록 보기와 해제처럼 한 입력 포트에 묶인 메서드까지 나눕니다. 이 규모에서는 파일만 늘고 얻는 것이 없습니다.

나누는 비용은 기계적인 리팩터링 한 번이고, 기준선보다 규칙 하나가 지키기 쉬워서 입력 포트마다 서비스 하나를 골랐습니다.

## 결과

- 서비스는 7개입니다. speech: `SpeakQuickChatService`, `FindMyVoiceChannelService`, `ManageVoiceService`. device: `IssuePairingCodeService`, `RegisterDeviceService`, `AuthenticateDeviceService`, `ManageOwnDevicesService`.
- 서비스는 자기 유스케이스에 필요한 출력 포트만 받습니다. 인바운드 어댑터는 서비스 클래스가 아니라 입력 포트 타입으로 주입받습니다. (빈 등록 방식은 ADR 0009 참고)
- 새 유스케이스는 기존 서비스를 키우지 않고 새 입력 포트와 새 서비스로 추가합니다.
- 한 입력 포트 안의 메서드(`ManageOwnDevicesUseCase` 의 목록·해제·모두 해제, `ManageVoiceUseCase` 의 목록·현재 목소리·바꾸기)는 계속 한 서비스에 둡니다. 읽기와 쓰기를 나눌지는 따로 판단합니다.
- `application/service` 의 클래스가 입력 포트를 하나만 구현하고, 이름이 그 입력 포트의 `UseCase` 를 `Service` 로 바꾼 것인지는 리뷰로 봅니다 ([ADR 0032](0032-archunit-checks-dependencies-only.md)).
- ~~먼저 온 채널 규칙(ADR 0007)과 그 잠금은 이제 `SpeakQuickChatService` 에만 있습니다.~~ ADR 0014 로 규칙은 `BotSeat`, 잠금은 디스코드 서버별로 `BotSeatPort` 에 있습니다. 서비스는 9개가 됐습니다(`LeaveEmptyVoiceChannelService`, `FollowBotMoveService` 추가).
- 서비스는 다른 서비스의 입력 포트를 받지 않고 출력 포트와 도메인만 씁니다. 두 서비스가 같은 규칙을 써야 하면 그 규칙을 도메인으로 옮깁니다. 예: 읽을 목소리를 정하는 규칙은 `VoiceCatalog` 에 있고 `ManageVoiceService` 와 `SpeakQuickChatService` 가 함께 씁니다. `ArchitectureTest` 가 서비스의 필드가 입력 포트 타입이 아닌지 확인합니다. (2026-10-15)
