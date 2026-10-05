# discord-quick-tts

단축키 한 번으로 입력창을 띄워 문장을 치면, 디스코드 봇이 내가 있는 음성 채널에서 읽어 주는 TTS 봇입니다.

```
[디스코드] /연결 → 나만 보이는 연결 코드
[PC] 첫 실행 때 코드 입력 → POST /api/devices → 이 PC 전용 기기 토큰 저장

[PC] Ctrl+Shift+Space → 입력 위젯 → Enter
        │  POST /api/quick-chat (Authorization: Bearer <기기 토큰>)
        ▼
[bot-server] 토큰 주인이 있는 음성 채널을 찾아 TTS 로 읽어 줌
```

## 모듈

| 모듈 | 내용 |
| --- | --- |
| `bot-server` | Spring Boot + JDA + LavaPlayer. `/연결` 명령, 기기 등록, quick chat API 를 맡습니다. |
| `desktop-client` | JavaFX 입력 위젯 + JNativeHook 전역 단축키 + 트레이 아이콘. |
| `common` | 클라이언트와 서버가 함께 쓰는 요청 형식과 상수. |

## 봇 서버 구조 (헥사고날)

```
bot-server/src/main/java/io/github/phs314/quicktts/bot/
├── domain/                 순수 도메인 모델 (QuickChatMessage, DiscordUserId, VoiceChannel, Speech)
│   └── device/             기기 연결 (PairingCode, Pairing, DeviceToken, Device)
├── application/
│   ├── port/in/            유스케이스 (SpeakQuickChat, IssuePairingCode, RegisterDevice, AuthenticateDevice)
│   ├── port/out/           바깥 세상에 대한 포트 (SpeechSynthesizer, VoiceChannelLocator, SpeechPlayer,
│   │                       PairingRepository, DeviceRepository)
│   └── service/            유스케이스 구현 (QuickChatService, DeviceRegistrationService)
├── adapter/
│   ├── in/web/             REST 컨트롤러
│   ├── in/discord/         /연결 슬래시 명령
│   ├── out/tts/            TTS 엔진 어댑터 (GoogleTranslateSpeechSynthesizer)
│   ├── out/discord/        JDA + LavaPlayer 어댑터
│   └── out/persistence/    기기 저장소 (H2, JdbcClient) 와 메모리 연결 코드 저장소
└── config/                 설정 값, JDA, 포트-어댑터 조립
```

- `domain` 과 `application` 은 스프링, JDA, LavaPlayer 를 모릅니다. 이 규칙과 "어댑터끼리 서로 모른다"는 규칙은 `ArchitectureTest`(ArchUnit)가 빌드 때마다 확인합니다.
- TTS 엔진은 `SpeechSynthesizer` 포트 뒤에 있습니다. 지금은 무료인 Google 번역 TTS 를 쓰고, 다른 엔진은 `adapter/out/tts` 에 어댑터를 추가한 뒤 `quicktts.tts.engine` 설정으로 고르면 됩니다.
- 기기 토큰은 서버에 SHA-256 해시로만 저장합니다. 연결 코드는 5분짜리 일회용이고 메모리에만 둡니다.

## 준비물

- JDK 25
- 디스코드 봇 (Discord Developer Portal 에서 만들고, `bot` 과 `applications.commands` 스코프, `View Channels`, `Connect`, `Speak` 권한으로 서버에 초대)

## 봇 서버 실행

1. `.env.example` 을 `.env` 로 복사하고 `DISCORD_TOKEN` 을 채웁니다. `.env` 는 커밋되지 않습니다.
2. 실행합니다. 등록된 기기는 `data/` 폴더의 H2 파일에 저장됩니다.

```bash
./gradlew :bot-server:bootRun
```

## 데스크톱 클라이언트 실행

```bash
./gradlew :desktop-client:run
```

처음 실행하면 연결 코드를 묻습니다. 디스코드에서 `/연결` 을 입력하면 나만 보이는 코드가 오니 그대로 붙여 넣으세요. 이후에는 `~/.discord-quick-tts/client.properties` 에 저장된 기기 토큰으로 자동 로그인됩니다. 봇 서버 주소는 기본 `http://localhost:8080` 이고, 배포판은 `-Dquicktts.server-url=...` 로 기본값을 바꿉니다.

음성 채널에 들어간 상태에서 `Ctrl+Shift+Space` 를 누르고 문장을 입력한 뒤 Enter 를 치면 봇이 읽어 줍니다. 종료는 트레이 아이콘의 Exit 메뉴로 합니다.

## 커밋 컨벤션

[Gitmoji](https://gitmoji.dev/) 이모지와 타입을 앞에 붙입니다.

```
<이모지> [<타입>] <요약>

<본문 (선택): 무엇을, 왜 바꿨는지>
```

- 요약은 한국어로 50자 안쪽, 끝에 마침표를 붙이지 않습니다.
- 한 커밋에는 한 가지 목적만 담습니다.
- 예: `✨ [feat] quick chat 입력창에 전송 기록 추가`

| 이모지 | 타입 | 언제 |
| --- | --- | --- |
| 🎉 | `init` | 프로젝트 시작 |
| ✨ | `feat` | 새 기능 |
| 🐛 | `fix` | 버그 수정 |
| ♻️ | `refactor` | 동작은 그대로 두고 구조 개선 |
| ✅ | `test` | 테스트 추가, 수정 |
| 📝 | `docs` | 문서 (README 등) |
| 🎨 | `style` | 포맷, 세미콜론 등 동작과 무관한 코드 모양 |
| 🔧 | `chore` | 빌드 설정, 의존성, 설정 파일 |
| 🔥 | `remove` | 코드나 파일 삭제 |
| 🚀 | `deploy` | 배포 관련 |
