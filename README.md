# discord-quick-tts

단축키 한 번으로 입력창을 띄워 문장을 치면, 디스코드 봇이 내가 있는 음성 채널에서 읽어 주는 TTS 봇입니다.

```
[PC] Ctrl+Shift+Space → 입력 위젯 → Enter
        │  POST /api/quick-chat (X-Api-Key)
        ▼
[bot-server] TtsEngine 로 음성 생성 → 내가 있는 음성 채널에서 재생
```

## 모듈

| 모듈 | 내용 |
| --- | --- |
| `bot-server` | Spring Boot + JDA + LavaPlayer. quick chat API 를 받아 음성 채널에서 읽어 줍니다. |
| `desktop-client` | JavaFX 입력 위젯 + JNativeHook 전역 단축키 + 트레이 아이콘. |
| `common` | 클라이언트와 서버가 함께 쓰는 요청 형식과 상수. |

## 봇 서버 구조 (헥사고날)

```
bot-server/src/main/java/io/github/phs314/quicktts/bot/
├── domain/                 순수 도메인 모델 (QuickChatMessage, DiscordUserId, VoiceChannel, Speech)
├── application/
│   ├── port/in/            유스케이스 (SpeakQuickChatUseCase)
│   ├── port/out/           바깥 세상에 대한 포트 (SpeechSynthesizer, VoiceChannelLocator, SpeechPlayer)
│   └── service/            유스케이스 구현 (QuickChatService)
├── adapter/
│   ├── in/web/             REST 컨트롤러
│   ├── out/tts/            TTS 엔진 어댑터 (GoogleTranslateSpeechSynthesizer)
│   └── out/discord/        JDA + LavaPlayer 어댑터
└── config/                 설정 값과 포트-어댑터 조립
```

- `domain` 과 `application` 은 스프링, JDA, LavaPlayer 를 모릅니다. 이 규칙과 "어댑터끼리 서로 모른다"는 규칙은 `ArchitectureTest`(ArchUnit)가 빌드 때마다 확인합니다.
- TTS 엔진은 `SpeechSynthesizer` 포트 뒤에 있습니다. 지금은 무료인 Google 번역 TTS 를 쓰고, 다른 엔진은 `adapter/out/tts` 에 어댑터를 추가한 뒤 `quicktts.tts.engine` 설정으로 고르면 됩니다.

## 준비물

- JDK 25
- 디스코드 봇 (Discord Developer Portal 에서 만들고, `bot` 스코프와 `Connect`, `Speak` 권한으로 서버에 초대)

## 봇 서버 실행

1. `.env.example` 을 `.env` 로 복사하고 `DISCORD_TOKEN`, `QUICKTTS_API_KEY` 를 채웁니다. `.env` 는 커밋되지 않습니다.
2. 실행합니다.

```bash
./gradlew :bot-server:bootRun
```

## 데스크톱 클라이언트 실행

```bash
./gradlew :desktop-client:run
```

처음 실행하면 `~/.discord-quick-tts/client.properties` 가 만들어집니다. 다음 값을 채운 뒤 다시 실행하세요.

- `server-url`: 봇 서버 주소 (기본 `http://localhost:8080`)
- `api-key`: 봇 서버의 `QUICKTTS_API_KEY` 와 같은 값
- `discord-user-id`: 내 디스코드 사용자 ID (디스코드 설정 > 고급 > 개발자 모드를 켠 뒤 내 프로필에서 "ID 복사")

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
