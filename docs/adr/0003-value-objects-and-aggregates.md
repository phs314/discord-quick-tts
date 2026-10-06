# 값 객체는 domain/vo 의 record, 애그리거트는 식별자로 비교하는 class

자바 `record` 는 모든 필드가 같으면 같은 객체로 칩니다. 값 객체에는 이것이 맞지만, 애그리거트에는 틀립니다. 같은 기기는 이름이 바뀌어도 같은 기기여야 하기 때문입니다. 그래서 둘을 다르게 만들기로 했습니다.

- **값 객체**는 각 컨텍스트의 `domain/vo` 패키지에 `record` 로 둡니다. 규칙에 맞지 않는 값은 생성자에서 `InvalidDomainValueException` 으로 막아, 만들어졌다면 올바른 값이 되게 합니다. 예: `PairingCode`, `DeviceToken`, `QuickChatMessage`, `VoiceId`, `DiscordUserId`.
- **애그리거트**는 `domain` 패키지에 프레임워크 애너테이션 없는 일반 `class`(POJO)로 둡니다. 필드는 `private final` 이고, `equals`/`hashCode` 는 식별자로만 판단합니다. `Device` 는 `id`(기기 ID), `Pairing` 은 `code`(연결 코드)가 식별자입니다. 접근자 이름은 record 와 같은 `id()`, `owner()` 모양을 씁니다.

## 결과

- 새로 만들 때는 `Device.register(...)`, `Pairing.issue(...)` 같은 정적 팩터리를 씁니다. `Device` 의 공개 생성자는 저장소에서 읽어 온 기기를 되살릴 때만 씁니다.
- 애그리거트를 저장했다 읽어 온 테스트는 `equals` 가 아니라 필드를 하나씩 비교해야 합니다 (`usingRecursiveComparison`).
- `speech` 컨텍스트에는 아직 애그리거트가 없습니다. 사용자가 고른 목소리는 디스코드 사용자와 `VoiceId` 의 짝으로만 저장합니다.
