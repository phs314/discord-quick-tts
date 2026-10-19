# 유스케이스(입력 포트) 하나에는 메서드 하나만 둔다

- 상태: 채택
- 날짜: 2026-10-19

입력 포트 11개 중 7개는 메서드가 하나였고, `ManageVoiceUseCase`, `ManageOwnDevicesUseCase`, `ManageGuildAllowlistUseCase`, `EnforceGuildAllowlistUseCase` 넷은 메서드가 셋씩이었습니다. ADR 0008 은 "메서드마다 서비스 하나" 를 놓고 "이 규모에서는 파일만 늘고 얻는 것이 없다" 며 고르지 않았고, 한 입력 포트 안의 메서드는 한 서비스에 두기로 했습니다. 그런데 메서드를 언제 한 입력 포트에 묶어도 되는지는 정하지 않아서, `Manage...` 처럼 넓은 이름에 메서드가 쌓일 수 있었습니다. 묶인 곳은 이름도 한 줄로 맞지 않았습니다. `ChangeVoiceCommand` 를 받는 메서드가 `ManageVoiceUseCase` 에 있고, `ManageGuildAllowlistService` 는 목록 보기에 필요 없는 `Clock` 과 이벤트 포트까지 받았습니다. 허용 목록에서 뺀 뒤 나가기만 하는 `GuildDisallowedListener` 는 쓰지 않는 시작 때 메서드 둘까지 보았습니다. 이 ADR 은 ADR 0008 의 "한 입력 포트 안의 메서드는 한 서비스에 둔다" 와 ADR 0029 결정 4번의 "한 유스케이스에 메서드가 여럿이면" 을 바꿉니다.

## 결정

1. **입력 포트(`...UseCase`) 하나에는 메서드 하나만 둡니다.** 그래서 유스케이스 하나 = 입력 포트 하나 = 메서드 하나 = 서비스 하나입니다(ADR 0008).
2. 이름은 메서드가 하는 일에서 짓고, Command·Query 이름과 맞춥니다: `ChangeVoiceUseCase.changeVoice(ChangeVoiceCommand)` → `ChangeVoiceService`. 메서드 이름은 지금처럼 짧은 동사도 됩니다(`UnlinkDeviceUseCase.unlink`, `LeaveEmptyVoiceChannelUseCase.leaveIfEmpty`).
3. 한 화면이나 한 슬래시 명령이 여러 일을 하면, 인바운드 어댑터가 유스케이스를 여럿 받습니다.
4. 서비스 둘이 같은 도우미를 쓰면 짧은 것은 서비스마다 둡니다(`requireOwner`, `currentAllowlist`). 규칙이 담긴 것은 지금처럼 도메인으로 옮깁니다(ADR 0008, ADR 0024).
5. 리뷰로 지킵니다(ADR 0032).

## 결과

- 메서드가 여럿이던 입력 포트 4개를 12개로 나눴습니다. 입력 포트와 서비스는 11개에서 19개가 됐습니다. 동작은 바뀌지 않습니다.

  지금:

  ```
  speech/application/port/in/   ManageVoiceUseCase (getVoices, getCurrentVoice, changeVoice)
  device/application/port/in/   ManageOwnDevicesUseCase (getDevices, unlink, unlinkAll)
  guild/application/port/in/    ManageGuildAllowlistUseCase (getAllowlist, allow, disallow)
                                EnforceGuildAllowlistUseCase (leaveIfNotAllowed, fillIfEmpty, leaveAllNotAllowed)
  ```

  바꾼 뒤:

  ```
  speech/application/port/in/   GetVoicesUseCase, GetCurrentVoiceUseCase, ChangeVoiceUseCase
  device/application/port/in/   GetDevicesUseCase, UnlinkDeviceUseCase, UnlinkAllDevicesUseCase
  guild/application/port/in/    GetAllowlistUseCase, AllowGuildUseCase, DisallowGuildUseCase,
                                LeaveNotAllowedGuildUseCase, FillEmptyAllowlistUseCase, LeaveAllNotAllowedGuildsUseCase
  ```

- 서비스는 그 메서드에 필요한 출력 포트만 받습니다. 예: `GetAllowlistService` 는 `Clock` 과 `GuildAllowlistEventPort` 를 받지 않고, `GetVoicesService` 는 `VoicePreferencePort` 를 받지 않습니다.
- `/목소리`, `/연결해제`, `/허용목록` 리스너는 유스케이스를 셋씩, `GuildJoinListener` 는 셋을 받습니다. `GuildDisallowedListener` 는 `LeaveNotAllowedGuildUseCase` 하나만 받습니다.
- 서비스 테스트도 서비스마다 하나입니다. 다른 유스케이스의 결과로 확인해야 하면 그 서비스를 함께 만들어 씁니다(예: `UnlinkDeviceServiceTest` 가 `GetDevicesService` 로 목록을 봅니다).
- 잃는 것: 파일이 늘고(입력 포트 8개, 서비스 8개, 서비스 테스트 6개 더), 봇 주인 확인 `requireOwner` 가 허용 목록 서비스 셋에, 허용 목록 읽기 `currentAllowlist` 가 서비스 셋에 같은 모양으로 있습니다.

## 고르지 않은 것

- 지금처럼 묶고 기준만 적기("부르는 쪽이 하나이고 같은 애그리거트를 보거나 바꾸는 메서드만 묶는다"): 파일은 그대로지만, 묶을지 말지를 새 기능마다 다시 판단해야 하고 이름이 한 줄로 맞지 않습니다.
- 보기와 바꾸기만 나누기(`GetVoiceUseCase` + `ChangeVoiceUseCase`): 본격 CQRS 는 하지 않기로 했고, `EnforceGuildAllowlistUseCase` 처럼 모두 바꾸는 일이면 나뉘지 않아 기준이 흐려집니다.
- `EnforceGuildAllowlistUseCase` 만 나누기: 부르는 쪽이 쓰지 않는 메서드를 보는 곳은 이것뿐이었지만, 규칙이 "메서드 하나" 보다 길어집니다.
