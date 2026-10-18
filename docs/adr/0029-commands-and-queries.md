# 유스케이스는 Command 나 Query 하나를 받는다

- 상태: 일부 대체됨
  - `AuthenticateDeviceQuery` 가 문자열을 받는 이유가 바뀜(다른 컨텍스트가 부르지 않음) → ADR 0031
  - 이름·인자 규칙은 리뷰로 지킴 → ADR 0032
- 날짜: 2026-10-18

유스케이스 입력을 언제 `...Command` 로 감싸는지는 "인자 3개 이상이면" 이라는 말로만 정해져 있었고 ADR 에는 없었습니다. 그런데 유일한 Command 인 `SpeakQuickChatCommand(speaker, message)` 는 인자가 둘이라 그 기준에도 맞지 않았습니다. 나머지 유스케이스는 `allow(DiscordUserId requester, GuildId guildId)` 처럼 값 객체를 그대로 받았습니다. 숫자 기준은 근거가 없어서, 새 유스케이스를 만들 때마다 감쌀지 말지를 다시 고민해야 했습니다.

## 결정

1. **상태를 바꾸는 유스케이스 메서드는 `...Command` 하나를 받습니다.** 필드가 하나여도 감쌉니다: `unlinkAll(UnlinkAllDevicesCommand command)`.
2. **조회 유스케이스 메서드는 `...Query` 하나를 받습니다**: `getDevices(GetDevicesQuery query)`, `findMyVoiceChannel(FindMyVoiceChannelQuery query)`, `authenticate(AuthenticateDeviceQuery query)`.
3. **받을 값이 없으면 인자 없이 둡니다.** 빈 record 는 만들지 않습니다: `getVoices()`, `leaveAllNotAllowed()`.
4. 이름은 메서드가 하는 일 + `Command` / `Query` 입니다(`ChangeVoiceCommand`, `GetCurrentVoiceQuery`). 한 유스케이스에 메서드가 여럿이면 메서드마다 하나씩 만듭니다(`ManageOwnDevicesUseCase` 는 `GetDevicesQuery`, `UnlinkDeviceCommand`, `UnlinkAllDevicesCommand`).
5. Command 와 Query 는 필드만 있는 record 입니다. 값 검사는 지금처럼 값 객체(`GuildId`, `DeviceName`, `QuickChatMessage`)가 하고, record 는 검사를 더하지 않습니다. 필드도 값 객체로 둡니다. `AuthenticateDeviceQuery` 만 device 도메인 타입 대신 헤더의 토큰 원문(`String`)을 받습니다. 빈 값이나 틀린 모양도 예외 대신 "주인 없음" 으로 돌려주기 위해서입니다.
6. 폴더는 `port/in/command`, `port/in/query` 입니다. 웹 DTO(`...Request`)는 그대로 두고, 인바운드 어댑터가 Command·Query 로 옮겨 담습니다.

## 결과

- Command 10개(`IssuePairingCodeCommand`, `RegisterDeviceCommand`, `UnlinkDeviceCommand`, `UnlinkAllDevicesCommand`, `LeaveIfNotAllowedCommand`, `AllowGuildCommand`, `DisallowGuildCommand`, `FollowBotMoveCommand`, `LeaveIfEmptyCommand`, `ChangeVoiceCommand`)와 Query 5개(`AuthenticateDeviceQuery`, `GetDevicesQuery`, `GetAllowlistQuery`, `FindMyVoiceChannelQuery`, `GetCurrentVoiceQuery`)를 더했습니다. 모든 유스케이스, 서비스, 인바운드 어댑터, 테스트가 이것을 씁니다.
- 유스케이스 입력 모양이 하나라서, 필드가 늘어도 유스케이스 인터페이스와 서비스 시그니처는 바뀌지 않고 부르는 곳의 `new ...Command(...)` 만 바뀝니다.
- 이름을 보면 상태를 바꾸는 일인지 조회인지 알 수 있습니다. ADR 0027 규칙 3번(조회는 `get` / `find`)과 짝입니다.
- 잃는 것은 필드 하나짜리 record 가 많아지는 것입니다(Command·Query 16개 중 9개). 부르는 곳이 `new LeaveIfEmptyCommand(guildId)` 처럼 한 번 더 감쌉니다.
- 리뷰로 지킵니다 ([ADR 0032](0032-archunit-checks-dependencies-only.md)). `query` 폴더의 클래스는 `Query` 로 끝나고, 유스케이스 메서드는 인자가 없거나 `port.in.command` / `port.in.query` 의 타입 하나만 받으며, 상태를 바꾸는 메서드는 Query 를 받지 않습니다.

## 고르지 않은 것

- 인자 3개 이상일 때만 Command: 숫자에 근거가 없고, 지금 있는 Command 도 이 기준에 맞지 않았습니다.
- 상태를 바꾸는 일만 Command, 조회는 값 그대로: 기준은 분명하지만 조회와 변경의 입력 모양이 달라지고, 조회에 조건이 늘어날 때 다시 정해야 합니다.
- Command 없이 값 객체만: 가장 단순하지만, 인자가 늘어나는 유스케이스가 생기면 그때 기준을 다시 정해야 합니다.
- 빈 record(`GetVoicesQuery()`): 담을 값이 없어서 감싸도 얻는 것이 없습니다.
