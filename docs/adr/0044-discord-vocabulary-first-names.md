# 이름은 디스코드 말을 먼저, 없으면 실무 표준을 쓴다

- 상태: 채택
- 날짜: 2026-11-01

봇 서버와 데스크톱 앱의 이름을 전부 훑어보니 세 가지가 섞여 있었습니다.

- 지어낸 비유: 봇이 들어가 있는 음성 채널을 `BotSeat`(봇 자리)라고 불렀습니다. 디스코드 문서에는 같은 것을 가리키는 말(Voice State)이 있는데 따로 지었습니다. `VoiceChannelLocator` 의 Locator 도 서비스 로케이터 패턴 이름이라 "음성 채널을 찾아 준다" 는 뜻으로는 잘 안 씁니다.
- 디스코드에서 다른 뜻인 말: `speaker` 는 디스코드에서 스테이지 채널의 발언자입니다. `owner` 는 디스코드에서 서버 주인이나 앱 주인이고, 우리 코드에도 봇 주인(`BotOwnerPort`)이 따로 있었습니다. 디스코드 문서를 읽은 사람은 반대로 이해합니다.
- 한 가지 일을 여러 말로 부름: 허용 목록 하나를 슬래시 명령은 add / remove, 유스케이스는 allow / disallow, 도메인은 permit, 처음 넣기는 fill 로 불렀습니다. 데스크톱 앱 단축키는 hotkey 와 shortcut 이 섞여 있었습니다. 오류 종류(`ErrorKind`)도 HTTP 말(`FORBIDDEN`, `CONFLICT`), gRPC 말(`UNAUTHENTICATED`), 지어낸 말(`INVALID_INPUT`, `EXTERNAL_FAILURE`)이 섞여 있었습니다.

## 결정

1. **이름은 이 순서로 고릅니다.**
   1. 디스코드 개발자 문서(API 객체, 게이트웨이 이벤트, 오류 코드)나 디스코드 앱 화면에 같은 뜻의 말이 있으면 그 말을 씁니다.
   2. 없으면 큰 회사가 쓰는 표준 말을 씁니다. TTS 는 Azure, Google, AWS, 웹 표준(Web Speech API), 그 밖에는 스프링, Google API 설계 지침, RFC 순서로 봅니다.
   3. 둘 다 없으면 뜻이 바로 읽히는 말로 짓고, 비유(seat, slot 같은)는 쓰지 않습니다. 제품 이름(quick chat)은 예외입니다.
2. **디스코드에 있는 말은 디스코드 뜻으로만 씁니다.** speaker, owner, session, stage, connection 을 다른 뜻으로 쓰지 않습니다.
3. **코드는 디스코드 API 말, 사람이 보는 글은 디스코드 한국어 화면 말** 입니다. 코드에서는 `guild` 이고 화면 글은 "디스코드 서버" 입니다(ADR 0027 의 1번 그대로).
4. **한 가지 일은 슬래시 명령, 유스케이스, 이벤트, 도메인 메서드에서 같은 동사로 부릅니다.**

## 결과

| 전 | 후 | 근거 |
|---|---|---|
| `BotSeat` (isFreeFor, sitIn, vacateIfEmpty, vacate, followBotMove) | `BotVoiceState` (canJoin, join, leaveIfEmpty, clear, applyUpdate) | 디스코드 Voice State, Update Voice State 의 join / move / leave |
| `BotSeatPort.withLockedSeat`, `BotSeatMemoryAdapter` | `BotVoiceStatePort.withLockedState`, `BotVoiceStateMemoryAdapter` | 위와 같음 |
| `FollowBotMove`, `VacateBotSeat` | `SyncBotVoiceState`, `ClearBotVoiceState` | 디스코드 소식을 상태에 반영하는 일은 sync |
| `speaker`, `SpeakerNotInVoiceChannelException`, `speaker-not-in-voice-channel` | `author`, `AuthorNotInVoiceChannelException`, `author-not-in-voice-channel` | 디스코드 Message 의 `author` |
| 기기의 `owner`, `isOwnedBy`, `findByOwner` | `userId`, `belongsTo`, `findByUserId` | 디스코드 owner 는 서버·앱 주인 |
| `VoiceChannelLocatorPort` (findCurrentChannel, findDetails, hasPeople) | `VoiceStatePort` (findUserVoiceChannel, findChannelDetails, hasHumanMembers) | 디스코드 Voice State, member, bot |
| `FindMyVoiceChannel` | `FindCurrentUserVoiceChannel` | 디스코드 "Get Current User Voice State". 없을 수 있어서 find(ADR 0027 3번) |
| `AllowGuild`, `DisallowGuild`, `GuildDisallowed`, `FillEmptyAllowlist` | `AddToAllowlist`, `RemoveFromAllowlist`, `GuildRemovedFromAllowlist`, `SeedAllowlist` | 슬래시 명령 add / remove, 처음 넣기는 seed |
| `GuildAllowlist.permits`, `notPermitted`, `needsFirstFill` | `allows`, `notAllowed`, `needsSeeding` | 위와 같음 |
| `ErrorKind` 의 `INVALID_INPUT`, `FORBIDDEN`, `CONFLICT`, `EXTERNAL_FAILURE` | `INVALID_ARGUMENT`, `PERMISSION_DENIED`, `FAILED_PRECONDITION`, `UNAVAILABLE` | gRPC 표준 상태 코드(Google API 설계 지침). `UNAUTHENTICATED` 는 그대로 |
| `DeviceUnauthorizedException`, `device-unauthorized` | `InvalidDeviceTokenException`, `invalid-device-token` | RFC 6750 의 `invalid_token`. 401 은 "권한 없음" 이 아니라 "인증 안 됨" |
| `LeaveIfEmptyCommand`, `LeaveIfNotAllowedCommand` | `LeaveEmptyVoiceChannelCommand`, `LeaveNotAllowedGuildCommand` | Command 이름은 유스케이스 이름과 같게 |
| `FailureReplyingListener` | `InteractionErrorHandler` | 디스코드 Interaction |
| `VoiceChannelResponse(serverName, serverIconUrl, …)`, `QuickChatRequest(text)`, `MAX_TEXT_LENGTH` | `guildName`, `guildIconUrl`, `content`, `MAX_CONTENT_LENGTH` | 디스코드 Guild, Message 의 `content` |
| 데스크톱 앱 `Hotkey`, Change Shortcut... | `Keybind`, Change Keybind... | 디스코드 설정 화면 Keybinds (이 ADR 전에 따로 바꿈) |

- 웹 JSON 이름과 오류 코드 문자열이 바뀌었습니다. 데스크톱 앱은 봇 서버를 같은 설치 파일 안에 넣어 함께 띄우므로(`LocalBotServer`) 새 판을 깔면 둘이 함께 바뀝니다. 서버를 따로 배포하기 전에 바꾸는 것이 가장 쌉니다. ADR 0027 은 배포된 클라이언트 때문에 JSON 이름을 그대로 뒀는데, 그 이유는 서버를 따로 배포한 뒤에야 생기므로 이번에 바꿉니다.
- 그대로 둔 이름: `guild`, `Device`(디스코드 설정의 기기 화면), `link` / `unlink`(Signal·WhatsApp 의 Linked devices), `Pairing`, `PairingCode`, `redeem`, `allowlist`(디스코드 AutoMod 의 `allow_list`), `BotOwner`(디스코드 앱 주인), `speech`, `SpeechSynthesizerPort`, `Voice`, `VoiceCatalog`, `GuildSpeechQueue`, `BusinessException`(한국 스프링 실무에서 가장 흔한 이름).

## 고르지 않은 것

- `VoiceSession`: 디스코드에서 session 은 음성 연결의 `session_id` 라 다른 뜻입니다.
- `VoiceConnection`: 디스코드 문서와 discord.js 에서 실제 음성 연결(웹소켓과 UDP)을 뜻합니다. 그건 JDA 의 `AudioManager` 가 맡는 어댑터 쪽 일이고, 도메인 객체는 연결이 아니라 상태만 듭니다.
- 메모리 어댑터에 스프링처럼 `InMemory` 붙이기: ADR 0039 의 `<대상><폴더>Adapter` 와 어긋나서 `BotVoiceStateMemoryAdapter` 로 둡니다.
- 디스코드 화면처럼 사용자 기기 해제를 "로그아웃" 으로 부르기: 디스코드 자기 로그인을 뜻해서 헷갈립니다.
- `ErrorKind` 를 HTTP 상태 이름으로 맞추기: 도메인 쪽 종류가 웹 말을 쓰게 됩니다. 디스코드 명령도 이 종류로 안내 문구를 고릅니다.
