# 문장 다듬기 규칙은 speech/domain/normalization 의 TextNormalizer 에 둔다

- 상태: 채택
- 날짜: 2026-11-01

문장을 소리 내 읽기 좋게 다듬는 규칙(ADR 0019)은 `QuickChatMessage.toSpokenText()` 안에 있었습니다. 규칙이 네 가지일 때는 값 객체의 메서드로 충분했지만, 잘 읽어 주는 일은 이 제품의 핵심이라 규칙은 앞으로 가장 많이 늘어날 코드입니다. 문장 하나의 검증(비었나, 200자 넘나)과 읽는 법이 한 클래스에 섞여 있으면 규칙이 늘수록 `QuickChatMessage` 가 커집니다. 그래서 커지기 전에 규칙의 자리와 이름을 정해 둡니다. 이 ADR 은 ADR 0019 의 "규칙을 별도 도메인 서비스 클래스로 빼지 않는다" 를 바꿉니다.

## 결정

1. **규칙은 `speech/domain/normalization/TextNormalizer` 에 둡니다.** 저장하는 것 없이 글만 바꾸는 도메인 서비스이고, `TextNormalizer.normalize(QuickChatMessage)` 가 `SpokenText` 를 돌려줍니다. 정적 메서드 하나라 스프링 빈이 아니고, 서비스는 바로 부릅니다.
2. **이름은 TTS 업계 말을 따릅니다(ADR 0044).** 디스코드에는 이 일을 가리키는 말이 없습니다. TTS 업계는 글을 소리 내 읽을 꼴로 바꾸는 단계를 text normalization 이라 부르고, 결과를 spoken form 이라 부릅니다. 그래서 패키지는 `normalization`, 클래스는 `TextNormalizer` 이고, 결과 `SpokenText` 는 이름 그대로 둡니다. 한국어로는 "읽기 다듬기" 라고 부릅니다.
3. **`QuickChatMessage` 는 보낸 문장의 검증만 맡습니다.** 앞뒤 공백 지우기, 빈 문장과 200자 넘는 문장 막기만 하고 `toSpokenText()` 는 없앱니다. `SpokenText` 는 지금처럼 비어 있으면 만들 수 없습니다.
4. **단어마다 읽는 법을 적는 사전이 생기면 그 객체는 `Lexicon` 이라 부릅니다.** W3C 의 Pronunciation Lexicon, AWS Polly 와 Azure 의 lexicon 과 같은 말입니다. 서버별 사전처럼 자기 테이블과 명령이 생기면 그때 normalization 을 컨텍스트로 올립니다.

규칙 자체(링크, 이모지, ㅋ·ㅎ·ㅇ·ㄴ·ㄱ, 빈 문장은 400)는 ADR 0019 그대로입니다.

## 결과

- 새 규칙은 `TextNormalizer` 에 더하고 `TextNormalizerTest` 에 예시를 더합니다.
- `QuickChatMessageTest` 에는 검증 테스트만 남습니다.
- speech 의 도메인 패키지에 하위 패키지가 하나 늘었습니다. ArchUnit 은 `speech.domain..` 전체를 도메인으로 보므로 규칙은 그대로입니다.

## 고르지 않은 것

- 이름을 `pronunciation`, `PronunciationRules` 로 하기: "발음" 은 단어마다 읽는 법을 적는 사전 쪽 말입니다. 지금 규칙은 글의 꼴을 바꾸는 쪽이라 normalization 이 맞고, 사전이 생겼을 때 두 이름이 섞이지 않습니다.
- 규칙을 따로 컨텍스트로 떼기: 상태도 저장도 없는 계산이라, 컨텍스트로 떼면 함수 하나를 부르려고 포트, 어댑터, 유스케이스가 생깁니다.
- `TextNormalizer` 를 스프링 빈으로 만들어 서비스에 주입하기: 바꿔 끼울 구현이 없고 도메인은 스프링을 모릅니다. 사전처럼 저장된 데이터가 필요해지면 서비스가 포트로 가져와 인자로 넘깁니다.
- `QuickChatMessage.toSpokenText()` 를 남기고 안에서 `TextNormalizer` 를 부르기: 같은 일을 하는 길이 둘이 되고, 값 객체가 도메인 서비스에 기대게 됩니다.
