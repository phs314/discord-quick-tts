# 출력 포트 이름은 Repository 대신 ...Port 로 끝낸다

DDD 책을 본 사람은 `DeviceRepository` 같은 이름을 먼저 떠올립니다. 하지만 출력 포트에는 저장소 말고도 TTS 엔진, 음성 재생, 음성 채널 찾기가 섞여 있어서, 이름만 보고는 그것이 포트(애플리케이션이 정한 약속)인지 어댑터(구현)인지 알기 어려웠습니다. 그래서 `application/port/out` 의 인터페이스 이름은 모두 `Port` 로 끝내기로 했습니다.

| 역할 | 이름 모양 | 예 |
| --- | --- | --- |
| 입력 포트 (`port/in`) | `...UseCase` | `SpeakQuickChatUseCase`, `RegisterDeviceUseCase` |
| 출력 포트 (`port/out`) | `...Port` | `DevicePort`, `PairingPort`, `SpeechSynthesizerPort`, `SpeechPlayerPort` |
| 어댑터 | 쓰는 기술 + 하는 일 | `JdbcDeviceRepository`, `InMemoryPairingRepository`, `LavaPlayerSpeechPlayer` |

## 결과

- 어댑터 이름에는 `Repository` 를 그대로 써도 됩니다. 기술 이름이 앞에 붙어 있어 포트와 헷갈리지 않습니다.
- 포트가 던지는 예외(`SpeechSynthesisException`)와 어댑터 안에서만 쓰는 인터페이스(`TtsEngine`)는 포트가 아니므로 `Port` 를 붙이지 않습니다.
- 이 이름 규칙은 `ArchitectureTest` 가 확인하지 않습니다. 리뷰에서 지킵니다.
