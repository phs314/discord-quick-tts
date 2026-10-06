# TTS 는 무료 비공식 엔진을 쓰고, 실패하면 Google 번역 목소리로 대신 읽는다

Google Cloud, Azure 같은 공식 TTS 는 API 키와 결제 등록이 필요해서, 혼자 쓰는 봇에는 부담이 컸습니다. 그래서 무료인 Microsoft Edge "소리 내어 읽기" 목소리(선희, 인준, 현수)와 Google 번역 목소리를 쓰기로 했습니다. 둘 다 공식 API 가 아니라서 언제든 막히거나 형식이 바뀔 수 있다는 것을 알고 고른 것입니다.

막힐 때를 대비해 엔진은 `SpeechSynthesizerPort` 뒤에 둡니다. `speech/adapter/out/tts` 의 `MultiEngineSpeechSynthesizer` 가 여러 `TtsEngine` 의 목소리를 한 목록으로 합치고, 고른 목소리의 엔진이 실패하면 Google 번역 목소리로 대신 읽어서 아무 소리도 안 나는 일을 줄입니다.

## 결과

- 도메인은 엔진을 모릅니다. `VoiceId`(예: `edge:ko-KR-SunHiNeural`, `google:ko`)는 같은지만 비교하고, 어느 엔진 것인지는 TTS 어댑터만 압니다.
- 엔진을 추가하거나 공식 TTS 로 바꾸려면 `TtsEngine` 구현을 하나 더 만들면 됩니다. 애플리케이션과 도메인은 바뀌지 않습니다.
- 목소리 목록은 `/목소리` 명령의 선택지로 그대로 쓰므로 디스코드 한도인 25개를 넘지 않게 합니다.
