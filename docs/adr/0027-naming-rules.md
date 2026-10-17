# 이름 규칙을 한곳에 모은다

접미사 규칙(`...UseCase`, `...Service`, `...Port`, `...Command`, `...Dto`, 포트 필드 이름)은 ADR 0004, 0008, 0010, 0012 에 나뉘어 있고 `ArchitectureTest` 가 지킵니다. 이름을 전부 훑어보니 어긋난 곳은 규칙이 없는 자리에 몰려 있었습니다.

- 같은 말이 두 뜻: 디스코드 서버를 거의 모든 곳에서 `guild` 로 부르는데 `VoiceChannelDetails` 만 `serverName` 이었고, 클라이언트에서 `server` 는 봇 서버를 뜻했습니다.
- 하는 일보다 약한 이름: `checkJoinedGuild` 는 확인만 할 것 같지만 봇을 서버에서 내보냈습니다.
- 같은 값을 부르는 변수가 둘: `GuildId` 를 `speech` 는 `guildId`, `guild` 는 `guild` 로 불렀습니다. `VoiceId` 도 `voice` 라서 같은 메서드 안의 `Voice voice` 와 이름이 같았습니다.
- 접두사 기준이 없음: 제품 전체를 가리키는 클래스(`QuickChatApi`, `QuickChatClient`, `QuickChatApp`)에 기능 이름 `QuickChat` 이 붙어 있었습니다.

## 결정

1. **디스코드 서버는 코드에서 언제나 `guild`** 입니다(JDA 도 `Guild`). `server` 는 봇 서버만 뜻합니다. 배포된 클라이언트가 읽는 웹 JSON 이름(`VoiceChannelResponse.serverName`, `serverIconUrl`)만 예외로 그대로 두고, 컨트롤러에서 옮겨 담습니다. 사용자에게 보이는 글은 지금처럼 "디스코드 서버" 입니다.
2. **다른 대상을 가리키는 식별자 변수는 `...Id` 로 부릅니다**: `GuildId guildId`, `VoiceId voiceId`. 다른 애그리거트는 객체가 아니라 ID 로 가리킨다는 규칙을 이름에서도 보이게 하고, 같은 자리에 있는 객체(JDA 의 `Guild guild`, 목소리 `Voice voice`)와 헷갈리지 않게 하려는 것입니다. 애그리거트와 record 의 구성 요소(`VoiceChannel.guildId`, `BotSeat.guildId`)와도 같은 이름이 됩니다. 역할 이름이 따로 있으면 역할로 부릅니다: `DiscordUserId owner`, `requester`, `speaker`, `VoiceId defaultVoice`.
3. **메서드 이름만 보고 하는 일을 알 수 있게 짓습니다.**
   - 바꾸는 일은 업무 동사입니다: `register`, `unlink`, `allow`, `changeVoice`. 동사는 실제로 하는 일만큼 세게 고릅니다. 서버에서 나가게 하면 `check` 가 아니라 `leaveIfNotAllowed` 처럼 "조건이면 나간다" 가 보이게 하고(`leaveIfEmpty` 와 같은 모양), 안내를 남기고 나가면 `leaveWithNotice` 입니다.
   - 조회도 동사입니다. 늘 값을 돌려주면 `get...`(목록이면 빈 목록), 없을 수 있으면 `find...`(`Optional`) 입니다: `getDevices`, `getAllowlist`, `getCurrentVoice`, `findMyVoiceChannel`. 유스케이스와 출력 포트가 같은 말을 씁니다.
   - 저장소 출력 포트는 `save` / `find...` / `delete...` 입니다. 꺼내면서 지우면 `findAndDelete`, 잠그고 일을 시키면 `withLockedSeat` 처럼 숨은 동작도 이름에 넣습니다.
   - 명사만 쓰는 건 객체가 자기 값을 돌려줄 때(record 접근자, `owner()`, `channel()`, `voices()`)뿐입니다. 참거짓은 `is`·`has`·`can` 또는 3인칭 동사(`permits`), 변환은 `to...`·`from`·`of`, 이벤트 받기는 `on...` 입니다.
   - 한 메서드가 이름에 없는 일을 하지 않습니다. 리스너를 등록하는 `register()` 가 봇을 서버에서 내보내기까지 하면 메서드를 나눕니다(`leaveNotAllowedGuildsOnStartup()`).
4. **출력 어댑터 이름은 `<기술><포트 이름에서 Port 를 뺀 것>`** 입니다: `JdaBotOwner`, `LavaPlayerSpeechPlayer`, `PropertiesGuildAllowlistSetting`. 애그리거트를 담는 저장소만 뒤에 `Repository` 를 붙입니다: `JdbcDeviceRepository`, `InMemoryPairingRepository`. 패키지는 기술 이름입니다: DB 는 `persistence`, 메모리는 `memory`, 그 밖에 `discord`, `tts`, `settings`. `ArchitectureTest` 가 확인합니다.
5. **접두사**: 제품 전체를 가리키면 `QuickTts`(`QuickTtsApi`, `QuickTtsClient`, `QuickTtsApp`), 단축키로 보내는 문장 기능만 `QuickChat`(`QuickChatMessage`, `QuickChatRequest`, `QuickChatController`, `QUICK_CHAT_PATH`) 입니다. 어댑터는 기술 접두사, 인터페이스에 `I`, 구현에 `Impl`, 추상 클래스에 `Abstract` 는 붙이지 않습니다.

## 결과

- 위 규칙에 맞게 이름을 바꿨습니다. `VoiceChannelDetails.guildName`·`guildIconUrl`, `EnforceGuildAllowlistUseCase.leaveIfNotAllowed`·`leaveAllNotAllowed`, `JoinedGuildPort.leaveWithNotice`, 조회 유스케이스 `getDevices`·`getAllowlist`·`getVoices`·`getCurrentVoice`, `BotSeatPort.withLockedSeat`, `PairingPort.findAndDelete`, `VoiceCatalog.chosenOrDefault`, `GuildJoinListener` 의 시작 때 확인 분리, `GuildId`·`VoiceId` 변수는 `guildId`·`voiceId`, `JdaJoinedGuilds` → `JdaJoinedGuild`, 메모리 저장소 둘은 `adapter/out/memory` 로, 클라이언트의 `QuickChat...` 넷은 `QuickTts...` 로, `QuickChatApi.PATH` 는 `QuickTtsApi.QUICK_CHAT_PATH` 로.
- 웹 경로와 JSON 이름은 바뀌지 않아서 이미 깔린 클라이언트도 그대로 동작합니다.
- 기계로 볼 수 있는 4번만 테스트로 막고, 나머지는 리뷰로 지킵니다.

## 고르지 않은 것

- 조회 유스케이스를 명사로(`devices(owner)`) 짓기: record 접근자와 같은 모양이지만, 그건 객체가 자기 값을 돌려줄 때의 관례입니다. 다른 값을 받아 찾아오는 유스케이스를 명사로 짓는 예제는 찾지 못했고, 이름만 봐서는 조회인지 알기 어렵습니다.
- 식별자 변수를 `Id` 없이 `guild`, `voice` 로 부르기: 짧지만 객체인지 ID 인지 이름으로 알 수 없고, 어댑터에서 JDA 의 `Guild` 를 `discordGuild` 처럼 따로 불러야 합니다.
- `guild` 를 모두 `server` 로 바꾸기: 사용자 말과 가까워지지만, JDA 의 `Guild` 와 계속 옮겨 담아야 하고 봇 서버와 같은 말이 됩니다.
- 웹 JSON 이름도 `guildName` 으로 바꾸기: 배포된 클라이언트가 깨지므로 API 버전을 올려야 합니다. 이름 하나 때문에 올릴 일은 아닙니다.
