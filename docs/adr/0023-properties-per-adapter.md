# 설정 값은 그 값을 쓰는 어댑터 패키지에 둔다

설정 값은 `config/QuickTtsProperties` 레코드 하나(`quicktts.*`)에 `discord`, `tts`, `guildAllowlist` 가 함께 들어 있었습니다. 그래서 speech 의 TTS 어댑터와 guild 의 설정 어댑터가 같은 레코드를 import 했고, guild 어댑터는 TTS 설정이 든 타입에 기대고 있었습니다. `ArchitectureTest` 는 `config` 를 계층 규칙에서 빼 두었기 때문에 이 의존은 어떤 규칙에도 걸리지 않았습니다. "guild 와 다른 컨텍스트는 서로 모른다"(ADR 0015)가 설정 파일 하나를 통해 새는 모양이었습니다.

구조 점검 보고서(G9)는 "세 번째 컨텍스트가 생기면 설정을 어댑터별로 나눈다" 고 적어 두었고, ADR 0015 로 guild 가 생겨 그 때가 왔습니다. 사용량 제한, 배포 서버 주소 같은 설정이 더 붙기 전에 나눕니다.

## 결정

- 설정 값 레코드는 그 값을 쓰는 어댑터 패키지에 둡니다.
  - `speech/adapter/out/tts/TtsProperties` (`quicktts.tts`): 기본 목소리, Edge 브라우저 버전
  - `guild/adapter/out/settings/GuildAllowlistProperties` (`quicktts.guild-allowlist`): 허용 목록을 쓸지
- `config` 에는 JDA 를 만드는 데 필요한 `DiscordProperties` (`quicktts.discord`, 봇 토큰)만 남깁니다. 토큰이 비어 있으면 켜질 때 멈추는 것도 그대로입니다.
- 어댑터는 `config` 의 설정이나 빈을 쓰지 않습니다. `ArchitectureTest` 의 `adaptersDoNotUseConfig` 가 확인합니다. `config` 가 어댑터를 쓰는 것(예: `DiscordListenerRegistrar` 가 디스코드 리스너를 모으는 것)은 조립 코드라 괜찮습니다.

## 고르지 않은 것

- 컨텍스트마다 설정 레코드 하나 (`speech/SpeechProperties` 같은 것): 지금은 컨텍스트마다 설정을 쓰는 어댑터가 하나뿐이라 결과가 같고, 어댑터가 둘이 되면 서로의 설정을 보게 됩니다.
- 설정을 출력 포트 뒤로 숨기기: guild 는 이미 `GuildAllowlistSettingPort` 로 그렇게 합니다. TTS 설정은 어댑터 자신이 쓰는 기술 설정이라 포트로 감쌀 이유가 없습니다.

## 결과

- 설정 키(`application.yml`, 환경변수 `QUICKTTS_DEFAULT_VOICE`, `QUICKTTS_GUILD_ALLOWLIST_ENABLED`, `DISCORD_TOKEN`)는 바뀌지 않습니다. `.env`, `server.env`, exe 를 쓰는 사람은 아무것도 바꾸지 않아도 됩니다.
- 새 설정은 그 값을 쓰는 어댑터 패키지에 `...Properties` 레코드로 더합니다. `@ConfigurationPropertiesScan` 이 찾아서 등록합니다.
- 기본 목소리를 디스코드 서버마다 다르게 하는 일(보고서 G7)이 오면, 그때는 TTS 설정이 아니라 애플리케이션의 값이 됩니다.
