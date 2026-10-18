# 데이터 상태로 정하는 규칙은 애그리거트와 값 객체에 둔다

애그리거트와 값 객체가 있었지만, 몇몇 규칙은 그 밖에 있었습니다. "내 기기만 해제할 수 있다"는 SQL 의 `where ... and discord_user_id = :owner` 에, "만료된 연결 코드는 못 쓴다"는 서비스의 `filter` 에 있었습니다. "허용 목록에 없는 디스코드 서버에서는 나간다"는 `EnforceGuildAllowlistService` 와 `ManageGuildAllowlistService` 두 서비스에 같은 조건으로 따로 적혀 있었습니다.

규칙이 SQL 에 있으면 도메인 테스트로 확인할 수 없고, 가짜 저장소(`FakeDevicePort`)가 같은 조건을 똑같이 흉내 내야만 서비스 테스트가 맞습니다. 규칙이 서비스에 있으면 같은 데이터를 쓰는 두 번째 유스케이스가 규칙을 다시 적게 됩니다. 허용 목록이 이미 그렇게 됐습니다.

## 결정

규칙을 어디에 둘지는 무엇으로 판단하느냐로 가립니다.

- **데이터의 상태로 판단하는 규칙**은 애그리거트나 값 객체에 둡니다. 서비스는 꺼내고, 도메인 메서드를 부르고, 저장만 합니다.
- **누가 부르는지로 판단하는 권한**(봇 주인만 허용 목록을 고친다)은 서비스에 둡니다. 데이터 자체의 규칙이 아니기 때문입니다.
- 저장소의 조건은 고르는 데만 씁니다(`findByOwner`, `deleteAllByOwner` 처럼 "내 것 모두"). 허락 여부를 정하는 조건은 넣지 않습니다.
- 도메인 규칙을 어기면 도메인이 예외를 던집니다. 이 예외는 애그리거트 옆, 각 컨텍스트의 `domain` 패키지 바로 아래에 둡니다. `shared` 의 `InvalidDomainValueException` 과 같은 자리입니다.

## 옮긴 것

| 규칙 | 전 | 후 |
| --- | --- | --- |
| 내 기기만 해제할 수 있다 | `DevicePort.deleteByIdAndOwner` 의 SQL 조건 | `Device.isOwnedBy`. 서비스는 `findById` 로 꺼내 주인이 아니면 없는 기기처럼 `false` 를 돌려주고, 맞으면 `delete(id)` |
| 만료된 연결 코드는 못 쓴다 | `RegisterDeviceService` 의 `filter(!isExpired)` | `Pairing.redeem(now)` 이 만료면 `InvalidPairingCodeException` 을 던지고, 아니면 코드를 받은 사용자를 돌려줌 |
| 목록에 있는 목소리만 고를 수 있다 | `ManageVoiceService` 의 `find(...).orElseThrow(...)` | `VoiceCatalog.choose` 가 `UnknownVoiceException` 을 던짐 |
| 허용 목록을 쓰면 목록에 있는 서버에만 머문다 | 두 서비스에 같은 조건 (`enabled && !exists`, `enabled && joined.contains`) | 값 객체 `GuildAllowlist.permits`, `notPermitted` |
| 허용 목록이 비었으면 지금 서버를 모두 넣는다 | `EnforceGuildAllowlistService` 의 `if` | `GuildAllowlist.needsFirstFill` |

- `InvalidPairingCodeException`, `UnknownVoiceException` 은 도메인이 던지게 되어 `application/exception` 에서 `domain` 으로 옮겼습니다. 서비스가 "없는 코드"일 때 같은 예외를 던지는 것은 그대로입니다.
- `DevicePort` 는 `deleteByIdAndOwner` 대신 `findById`, `delete` 를, `AllowedGuildPort` 는 쓰지 않게 된 `exists` 를 뺐습니다. 허용 목록은 봇 주인이 넣은 서버 몇 개라서 매번 `findAll` 로 읽어도 됩니다.

## 그대로 둔 것

| 위치 | 이유 |
| --- | --- |
| `ManageGuildAllowlistService.requireOwner` (봇 주인 확인) | 누가 부르는지로 판단하는 권한 |
| `SpeakQuickChatService` 가 `BotSeat.isFreeFor` 로 먼저 묻고 거절 | 규칙은 이미 `BotSeat` 에 있음. 묻기와 앉기 사이에 디스코드 음성 채널 들어가기가 있어서, 들어가지 못하면 자리를 그대로 두려고 둘로 나눈 것(ADR 0014). 서비스는 거절할 때 채널 이름을 붙이는 일만 함 |
| `AuthenticateDeviceService` 의 빈 토큰 확인 | 인증 실패를 예외 대신 빈 값으로 돌려주려는 입력 처리 |
| `PairingPort.take` 의 꺼내며 지우기 | "한 번만 쓴다"를 동시 요청에도 지키려면 저장소가 한 번에 해야 함 |
| `JdaVoiceChannelLocator.hasPeople` 의 "봇이 아닌 사람" | 디스코드 멤버를 도메인의 "사람"으로 바꾸는 번역 |
| `UnlinkCommandListener` 의 메뉴 24개 한도 | 디스코드 선택 메뉴의 한도 |
| `MultiEngineSpeechSynthesizer` 의 기본 목소리, 대체 엔진 | 기본 목소리는 제품 규칙이라 옮길 후보지만 출력 포트 모양이 바뀌는 일이라 따로 다룸. 대체 엔진은 기술 문제라 어댑터가 맞음 |

## 결과

- 도메인의 예외를 `domain` 패키지 바로 아래에만 두는지는 리뷰로 봅니다 ([ADR 0032](0032-archunit-checks-dependencies-only.md)).
- 도메인 테스트가 생겼습니다: `DeviceTest`, `PairingTest`, `GuildAllowlistTest`, `VoiceCatalogTest` 의 고르기.
- `FakeDevicePort` 는 더 이상 주인 조건을 흉내 내지 않습니다.
