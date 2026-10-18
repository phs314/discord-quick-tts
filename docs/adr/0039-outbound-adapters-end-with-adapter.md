# 출력 포트를 구현한 어댑터는 이름을 Adapter 로 끝낸다

- 상태: 채택
- 날짜: 2026-10-18

출력 어댑터 이름은 `<기술><포트 이름에서 Port 를 뺀 것>`(`JdaBotOwner`, `LavaPlayerSpeechPlayer`)이고, 저장소만 뒤에 `Repository` 를 붙였습니다(`JpaDeviceRepository`, ADR 0027 4번, ADR 0038). 이 이름에는 두 가지 불편이 있었습니다.

- 어느 클래스가 포트를 구현하는지 이름만으로는 알 수 없습니다. `tts` 폴더에는 `MultiEngineSpeechSynthesizer`, `EdgeReadAloudEngine`, `GoogleTranslateEngine` 이 나란히 있는데, 포트를 구현하는 것은 첫째 하나뿐입니다.
- `persistence` 폴더에서 포트 구현(`JpaDeviceRepository`)과 Spring Data 인터페이스(`SpringDataDeviceRepository`)가 모두 `Repository` 로 끝나고, 한 대상의 네 파일이 탐색기에서 흩어져 보입니다(`DeviceJpaEntity`, `DevicePersistenceMapper`, `JpaDeviceRepository`, `SpringDataDeviceRepository`).

헥사고날 구조의 스프링 프로젝트는 포트 구현을 `...Adapter` 로 끝내는 경우가 가장 많습니다. 이 ADR 은 ADR 0027 4번과 5번의 "어댑터는 기술 접두사", ADR 0038 1번의 `Jpa<이름>Repository`·`SpringData<이름>Repository` 이름을 바꿉니다.

## 결정

1. **출력 포트를 구현한 클래스 이름은 `<포트 이름에서 Port 를 뺀 것><폴더 이름>Adapter`** 입니다: `DevicePersistenceAdapter`, `PairingMemoryAdapter`, `BotOwnerDiscordAdapter`, `SpeechPlayerDiscordAdapter`. 폴더 뜻이 이미 이름에 있으면 한 번만 씁니다: `GuildAllowlistEventAdapter`(event), `GuildAllowlistSettingAdapter`(settings), `SpeechSynthesizerAdapter`(tts).
2. **포트를 구현하지 않는 클래스에는 `Adapter` 를 붙이지 않습니다.** 어댑터 안에서만 쓰는 도우미(`GuildSpeechQueue`, `AudioPlayerSendHandler`), TTS 엔진(`EdgeReadAloudEngine`, `GoogleTranslateEngine`, ADR 0006), 설정(`...Properties`, ADR 0023)은 이름을 그대로 둡니다.
3. **Spring Data 인터페이스는 `<이름>SpringDataRepository`** 입니다. 같은 `persistence` 폴더의 네 파일이 모두 대상 이름으로 시작합니다: `DeviceJpaEntity`, `DevicePersistenceAdapter`, `DevicePersistenceMapper`, `DeviceSpringDataRepository`. 엔티티(`<이름>JpaEntity`)와 매퍼(`<이름>PersistenceMapper`)는 ADR 0038 그대로입니다.
4. 이름 규칙이라 ArchUnit 이 아니라 리뷰로 지킵니다(ADR 0032).

## 결과

- 포트 구현 12개, Spring Data 인터페이스 3개, 어댑터 테스트 6개의 이름을 바꿨습니다. 동작, 빈 구성, 테이블은 바뀌지 않습니다.

  | 폴더 | 전 | 후 |
  | --- | --- | --- |
  | device/persistence | `JpaDeviceRepository` | `DevicePersistenceAdapter` |
  | device/memory | `InMemoryPairingRepository` | `PairingMemoryAdapter` |
  | guild/discord | `JdaBotOwner`, `JdaJoinedGuild` | `BotOwnerDiscordAdapter`, `JoinedGuildDiscordAdapter` |
  | guild/event | `SpringGuildAllowlistEvent` | `GuildAllowlistEventAdapter` |
  | guild/persistence | `JpaAllowedGuildRepository` | `AllowedGuildPersistenceAdapter` |
  | guild/settings | `PropertiesGuildAllowlistSetting` | `GuildAllowlistSettingAdapter` |
  | speech/discord | `LavaPlayerSpeechPlayer`, `JdaVoiceChannelLocator` | `SpeechPlayerDiscordAdapter`, `VoiceChannelLocatorDiscordAdapter` |
  | speech/memory | `InMemoryBotSeatRepository` | `BotSeatMemoryAdapter` |
  | speech/persistence | `JpaVoicePreferenceRepository` | `VoicePreferencePersistenceAdapter` |
  | speech/tts | `MultiEngineSpeechSynthesizer` | `SpeechSynthesizerAdapter` |
  | 각 persistence | `SpringData<이름>Repository` | `<이름>SpringDataRepository` |

- 이름에서 기술(Jpa, Jda, LavaPlayer)이 빠지고 폴더 이름만 남습니다. 어떤 기술을 쓰는지는 폴더와 클래스 설명에서 봅니다. 한 폴더에 기술이 둘 생기면(예: JPA 와 JdbcClient 를 함께 쓰는 저장소) 그때 이름에 기술을 더합니다.
- 로그의 로거 이름(`JoinedGuildDiscordAdapter`, `SpeechSynthesizerAdapter`, `SpeechPlayerDiscordAdapter`)이 바뀝니다. 로거 이름으로 거르는 설정은 없습니다.
- 옛 ADR 본문의 클래스 이름은 ADR 0034 에 따라 고치지 않습니다. 지금 이름은 저장소 README 를 봅니다.

## 고르지 않은 것

- 지금 이름 그대로 두기: 포트 구현과 도우미가 이름으로 구분되지 않고, `persistence` 폴더에 `Repository` 가 둘인 채로 남습니다.
- 기술 이름을 넣어 `JpaDeviceAdapter`, `JdaBotOwnerAdapter` 처럼 짓기: 기술은 남지만 같은 폴더 파일이 대상 이름으로 모이지 않고, 폴더 이름(`persistence`, `discord`)과 다른 말이 하나 더 생깁니다.
- 출력 포트를 `Load...Port`/`Save...Port` 로 쪼개기: 애그리거트마다 포트 하나(ADR 0004)로 충분해서 이번에 함께 바꾸지 않습니다.
- 엔티티를 `<이름>Entity`, 매퍼를 `<이름>Mapper` 로 줄이기: DDD 의 엔티티(`Device`)와 JPA 엔티티가, 웹 매퍼(ADR 0030)와 퍼시스턴스 매퍼가 이름으로 구분되지 않습니다.
