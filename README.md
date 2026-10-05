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

## 봇 서버 구조 (바운디드 컨텍스트 + 헥사고날)

봇 서버는 두 바운디드 컨텍스트로 나뉘고, 각 컨텍스트 안은 헥사고날(도메인 / 포트 / 어댑터)로 나뉩니다.

| 컨텍스트 | 다루는 것 |
| --- | --- |
| `speech` (핵심) | 누가 무슨 문장을 어느 음성 채널에서 읽게 하나 |
| `device` (지원) | 이 PC 가 어느 디스코드 사용자의 것인가 (`/연결` 코드 발급, 기기 등록, 기기 인증) |
| `shared` | 두 컨텍스트가 함께 쓰는 공유 커널 (`DiscordUserId`, 도메인 검증 예외) |

```
bot-server/src/main/java/io/github/phs314/quicktts/bot/
├── speech/
│   ├── domain/                 QuickChatMessage, VoiceChannel, Speech, Voice
│   ├── application/
│   │   ├── port/in/            SpeakQuickChatUseCase, ManageVoiceUseCase
│   │   ├── port/out/           SpeechSynthesizer, VoiceChannelLocator, SpeechPlayer, VoicePreferenceRepository
│   │   └── service/            QuickChatService, VoiceService
│   └── adapter/
│       ├── in/web/             quick chat REST 컨트롤러
│       ├── in/discord/         /목소리 슬래시 명령
│       ├── out/tts/            TTS 엔진 (Edge, Google 번역) 과 엔진 고르기
│       ├── out/discord/        JDA + LavaPlayer 로 채널 찾기, 재생
│       └── out/persistence/    사용자별 목소리 저장소 (H2)
├── device/
│   ├── domain/                 PairingCode, Pairing, DeviceToken, Device
│   ├── application/
│   │   ├── port/in/            IssuePairingCode, RegisterDevice, AuthenticateDevice (공개 입구)
│   │   ├── port/out/           PairingRepository, DeviceRepository
│   │   └── service/            DeviceRegistrationService
│   └── adapter/
│       ├── in/web/             기기 등록 REST 컨트롤러
│       ├── in/discord/         /연결 슬래시 명령
│       └── out/persistence/    기기 저장소 (H2, JdbcClient), 메모리 연결 코드 저장소
├── shared/                     공유 커널과 공통 예외 처리
└── config/                     설정 값, JDA, 포트-어댑터 조립
```

`ArchitectureTest`(ArchUnit)가 빌드 때마다 다음 규칙을 확인합니다.

- 각 컨텍스트의 `domain` 과 `application` 은 스프링, JDA, LavaPlayer 를 모르고, 어댑터끼리는 서로 모릅니다.
- `speech` 는 `device` 를 공개 입구(`device.application.port.in`)로만 씁니다. quick chat 요청의 기기 토큰 주인을 물을 때가 유일한 접점입니다.
- `device` 는 `speech` 를 모르고, `shared` 는 어느 컨텍스트도 모릅니다.

그 밖에:

- TTS 엔진은 `SpeechSynthesizer` 포트 뒤에 있습니다. 지금은 무료인 Microsoft Edge "소리 내어 읽기" 목소리(선희, 인준, 현수)와 Google 번역 목소리를 쓰고, Edge 가 실패하면 Google 번역 목소리로 대신 읽습니다. 엔진을 추가하려면 `speech/adapter/out/tts` 에 `TtsEngine` 구현을 하나 더 만들면 됩니다. 둘 다 공식 API 가 아니라서 언제든 막힐 수 있습니다.
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

연결한 PC 를 끊으려면 디스코드에서 `/연결해제` 를 입력하고 메뉴에서 PC 를 고르세요. 목록에는 각 PC 의 컴퓨터 이름이 보입니다. 끊긴 PC 의 클라이언트는 다음에 보낼 때 연결 코드를 다시 묻습니다.

읽어 주는 목소리는 사람마다 디스코드에서 `/목소리` 로 바꿀 수 있습니다. 목소리를 고르지 않으면 지금 목소리와 목록을 보여 줍니다. 아무도 고르지 않았을 때의 기본 목소리는 `QUICKTTS_DEFAULT_VOICE` 로 정합니다(기본: 선희).

## 클라이언트 exe 만들기 (Windows)

자바를 설치하지 않은 PC 에서도 돌아가도록 자바 런타임까지 넣은 `QuickTTS.exe` 를 만듭니다. JDK 25 에 들어 있는 `jpackage` 를 쓰므로 다른 도구는 필요 없습니다.

```bash
./gradlew :desktop-client:packageZip
```

`desktop-client/build/distributions/QuickTTS-0.1.0-windows.zip` 이 생깁니다. 압축을 풀고 `QuickTTS/QuickTTS.exe` 를 실행하면 됩니다. 압축하지 않은 폴더만 필요하면 `packageExe` 를 쓰세요(`desktop-client/build/jpackage/image/QuickTTS`).

- 기본 봇 서버 주소는 `http://localhost:8080` 입니다. 다른 곳에 띄운 서버를 쓰려면 `-PserverUrl=https://...` 를 붙여 만듭니다.
- 서명하지 않은 exe 라서 처음 실행할 때 Windows 가 "알 수 없는 게시자" 경고를 띄웁니다. "추가 정보" → "실행" 으로 넘어가면 됩니다.

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

이 형식은 PR 마다 CI 가 검사합니다. 커밋할 때 바로 걸러지게 하려면 저장소를 클론한 뒤 한 번만 훅을 켜 두세요.

```bash
git config core.hooksPath .githooks
```
