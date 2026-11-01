# speech 에서 사용자 설정(settings)과 음성 만들기(synthesis)를 떼어 낸다

- 상태: 채택
- 날짜: 2026-11-01

speech 하나가 세 가지 일을 했습니다. 문장을 다듬어 음성 채널에서 읽어 주는 일, 사람마다 고른 목소리를 기억하는 일(`/목소리`, `voice_preference` 테이블), TTS 엔진을 감싸 목소리 목록과 음성을 내놓는 일(`adapter/out/tts`)입니다. 세 일은 바뀌는 이유가 다릅니다. 읽기 속도 같은 사용자 설정이 늘면 둘째가, 유료 엔진이 붙으면 셋째가 커지고, 둘 다 첫째와 상관없이 바뀝니다. 그래서 speech 가 커지기 전에 둘을 따로 떼어 둡니다. 이 ADR 은 ADR 0002 의 컨텍스트 목록에 둘을 더하고, ADR 0023 의 설정 어댑터 폴더 이름(`settings`)을 `config` 로 바꿉니다.

## 결정

1. **컨텍스트는 다섯입니다.** 모두 지금처럼 봇 서버 안의 패키지이고, 안은 `domain` / `application` / `adapter` 입니다.
   - `speech`(핵심): 누가 무슨 문장을 어느 음성 채널에서 읽게 하나. 봇 음성 상태, 문장 다듬기, 재생.
   - `settings`(지원, 새로): 사용자가 고른 설정. 애그리거트 `UserSettings` 하나이고 지금은 목소리 하나를 듭니다. 이름은 디스코드의 User Settings 를 따릅니다(ADR 0044).
   - `synthesis`(일반, 새로): TTS 엔진과 목소리 목록. 엔진 컴포지트(`SpeechSynthesizerAdapter`, ADR 0006·0037)는 그대로 옮깁니다.
   - `device`, `guild`: 그대로입니다.
2. **컨텍스트 지도**: speech → settings, synthesis / settings → synthesis. synthesis 는 아무도 부르지 않습니다. 부르는 자리는 ADR 0040 대로 `adapter/out/<상대>` 하나입니다.
   - speech 의 `UserVoicePort`(읽을 목소리) ← `adapter/out/settings/UserVoiceSettingsAdapter` → settings 의 `GetUserSettingsUseCase`
   - speech 의 `TextToSpeechPort`(다듬은 문장을 음성으로) ← `adapter/out/synthesis/TextToSpeechSynthesisAdapter` → synthesis 의 `SynthesizeSpeechUseCase`
   - settings 의 `VoiceChoicesPort`(고를 수 있는 목소리) ← `adapter/out/synthesis/VoiceChoicesSynthesisAdapter` → synthesis 의 `GetVoicesUseCase`
3. **다른 컨텍스트에 내주는 것은 `...Dto` 이고, 받는 쪽 어댑터가 자기 값 객체로 바꿉니다.** `UserSettingsDto`, `VoiceCatalogDto`, `AudioDto` 셋입니다. 값은 원시 타입과 shared 의 값 객체만 담습니다.
4. **`VoiceId` 는 shared 로 옮깁니다.** 세 컨텍스트가 같은 목소리를 같은 값으로 가리키고, 엔진이 정한 문자열이라 컨텍스트마다 뜻이 다를 일이 없습니다(`DiscordUserId`, `GuildId` 와 같은 이유).
5. **같은 것을 컨텍스트마다 자기 말로 따로 듭니다.** 목소리 하나는 synthesis 의 `Voice` 와 settings 의 `VoiceChoice`(`/목소리` 의 선택지), 목소리 목록은 synthesis 의 `VoiceCatalog` 와 settings 의 `VoiceChoices`, 음성은 synthesis 와 speech 의 `Audio` 입니다. 모양이 같아도 상대 도메인을 import 하면 경계가 무너지므로 공유하지 않습니다.
6. **"고른 게 없으면 기본 목소리" 는 settings 가 정합니다.** `VoiceChoices.chosenOrDefault` 가 고른 목소리가 목록에서 사라진 경우까지 맡고, speech 는 정해진 `VoiceId` 하나만 받습니다. 무엇이 기본 목소리인지는 엔진 설정(`quicktts.tts.default-voice`)이라 synthesis 가 알려 줍니다.
7. **이름**(ADR 0044 의 이름 전수조사): `VoicePreference` → `UserSettings`, `voice_preference` 테이블 → `user_settings`(Flyway V3, 이름만 바꿈), `VoicePreferencePort.findVoice / saveVoice` → `UserSettingsPort.find / save`, `GetCurrentVoice` → `GetUserSettings`, 음성 `Speech(audio, fileExtension)` → `Audio(content, format)`. 사람별 설정을 settings 로 부르므로 앱 설정 값 폴더 `guild/adapter/out/settings` 와 `GuildAllowlistSettingPort` 는 `config`, `GuildAllowlistConfigPort` 로 바꿉니다.
8. **예외**: `SpeechSynthesisException` 은 synthesis 로, `UnknownVoiceException` 은 settings 로 옮깁니다. 재생할 수 없는 음성은 speech 의 `UnplayableAudioException` 이 알리고, 사용자에게는 전처럼 음성을 만들지 못했다는 오류(`speech-synthesis-failed`, 502)로 보입니다.

## 결과

- 사용자가 보는 동작과 웹 API 는 바뀌지 않습니다. 고른 목소리는 테이블 이름만 바뀌어 그대로 남습니다(옛 H2 파일에서 올라오는 것도 테스트가 봅니다).
- quick chat 한 번에 speech 가 settings 와 synthesis 를 한 번씩 부릅니다. 한 프로세스 안의 메서드 호출이라 느려지지 않습니다.
- `ArchitectureTest` 의 컨텍스트 지도에 settings, synthesis 와 위의 짝이 들어가고, 두 컨텍스트에도 헥사고날 규칙이 붙습니다.
- 테스트 대역도 포트를 따라 나뉩니다: speech 의 `FakeUserVoicePort`, `FakeTextToSpeechPort`, settings 의 `FakeUserSettingsPort`, `FakeVoiceChoicesPort`, synthesis 의 `FakeSpeechSynthesizerPort`. API 통합 테스트는 전처럼 synthesis 의 엔진 포트만 바꿔서 세 컨텍스트를 잇는 길까지 실제로 돕니다.

## 고르지 않은 것

- 목소리 고르기를 speech 에 두고 synthesis 만 떼기: 사용자 설정이 늘 때마다 읽어 주기와 상관없는 테이블과 명령이 speech 에 쌓입니다.
- settings 와 synthesis 를 한 컨텍스트로: 사람이 무엇을 골랐는지와 엔진이 무엇을 낼 수 있는지는 바뀌는 이유가 다릅니다. 유료 엔진을 붙일 때 사용자 설정을 건드리지 않게 하려는 것입니다.
- `Voice`, `Audio` 를 shared 에 두고 함께 쓰기: 파일은 줄지만 한 컨텍스트가 모양을 바꾸면 나머지가 모두 따라 바뀝니다. shared 에는 뜻이 바뀌지 않는 ID 만 둡니다.
- 기본 목소리 고르기를 synthesis 에 두기(`synthesize(text, 고른 목소리 또는 없음)`): speech 는 편해지지만 `/목소리` 의 "지금 목소리" 도 같은 규칙이 필요해서, 사람의 선택을 푸는 일은 settings 한 곳에 둡니다.
- speech 의 포트도 `SpeechSynthesizerPort` 로 부르기: 같은 이름의 포트와 어댑터가 두 컨텍스트에 생겨 스프링 빈 이름이 겹치고 읽을 때도 헷갈립니다. speech 쪽은 하는 일 그대로 `TextToSpeechPort` 로 부릅니다.
