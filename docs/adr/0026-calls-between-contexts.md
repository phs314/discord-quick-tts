# 컨텍스트 사이 호출은 컨텍스트 지도와 공개 입구로만 한다

컨텍스트끼리 부르는 길은 `speech` 의 웹 어댑터(`QuickChatController`)가 `device` 의 `AuthenticateDeviceUseCase` 를 부르는 것 하나였습니다. ArchUnit 도 그 짝 하나만 열어 두고, 나머지는 짝마다 따로 막았습니다(`speech` 는 `device` 공개 입구만, `device` 는 `speech` 를 모름, `guild` 는 둘과 서로 모름). 그래서 두 가지가 비어 있었습니다.

- 서비스가 일하는 도중에 다른 컨텍스트의 값이 필요할 때 쓸 길이 없습니다. 서비스는 입력 포트를 필드로 가질 수 없고(ADR 0008), 그 규칙은 다른 컨텍스트의 입력 포트도 막습니다.
- 컨텍스트를 새로 만들면 짝마다 쓴 규칙에 들어 있지 않아서, 아무 컨텍스트나 마음대로 불러도 빌드가 통과합니다.

## 결정

- **컨텍스트 지도**를 `ArchitectureTest.CONTEXT_MAP` 한 곳에 둡니다. "누가 누구를 부를 수 있나" 가 여기에만 있습니다. 지금은 `speech → device` 하나입니다. `guild` 는 다른 컨텍스트와 서로 모르고(ADR 0015), `shared` 는 어느 컨텍스트도 모릅니다(ADR 0002).
- `bot` 바로 아래 패키지는 지도에 있는 컨텍스트이거나 `shared`, `config` 여야 합니다. 새 컨텍스트를 만들면 지도에 넣어야 빌드가 통과합니다.
- 다른 컨텍스트는 그 **공개 입구**(`application.port.in` 의 `...UseCase`, `...Command`, `...Query`, `...Dto`)로만 부릅니다. 상대의 서비스, 도메인, 출력 포트는 쓰지 않습니다.
- 부르는 자리는 둘입니다.
  - **인바운드 어댑터**: 요청을 받으면서 바로 필요한 것. 예: `QuickChatController` 가 기기 토큰의 주인을 묻는 것.
  - **`adapter/out/<상대 컨텍스트>/`**: 서비스가 일하는 도중에 필요한 것. 서비스는 내 `port/out` 에 **내 말로 된** 출력 포트(`...Port`)를 두고 부릅니다. 그 포트의 구현이 `adapter/out/<상대>/` 에서 상대의 입력 포트를 부르고, 상대의 `...Dto` 를 내 값 객체로 바꿉니다. 서비스와 도메인은 상대 컨텍스트를 전혀 모르고, 상대 모델이 바뀌면 이 어댑터 한 곳만 고칩니다. 지금은 이렇게 쓰는 곳이 없어 길만 열어 둡니다.
- **도메인 이벤트**(비동기, "이 일이 끝났다" 알리기)는 모양만 정합니다. 결과 값이 지금 필요하면 위의 동기 호출을 쓰고, 알림을 받는 쪽이 실패해도 내 일은 성공이어야 하면 이벤트를 씁니다. 처음 필요해지면 이렇게 만듭니다.
  - 이벤트는 내 컨텍스트 `domain/event` 의 과거형 이름 record 입니다(예: `DeviceUnlinked`). 값 객체만 담습니다.
  - 서비스가 저장한 뒤 출력 포트(`...EventPort`)로 발행합니다. 스프링 `ApplicationEventPublisher` 는 그 포트를 구현한 어댑터 안에만 둡니다.
  - 받는 쪽 컨텍스트는 인바운드 어댑터(`adapter/in/event`)로 받아 자기 입력 포트를 부릅니다. 트랜잭션이 커밋된 뒤에 받습니다(`@TransactionalEventListener`).

## 결과

- `ArchitectureTest` 의 짝마다 쓴 규칙 넷(`guildIsIndependentOfOtherContexts`, `otherContextsDoNotKnowGuild`, `speechUsesDeviceOnlyThroughItsPublicApi`, `deviceDoesNotKnowSpeech`)을 두 규칙(`everyContextIsOnTheContextMap`, `contextsMeetOnlyAtPublicEntrances`)으로 바꿨습니다. 계층 규칙(`onionArchitecture`)은 어댑터가 다른 컨텍스트의 공개 입구를 쓰는 의존을 빼고, 그 의존은 컨텍스트 규칙이 봅니다.
- 서비스가 다른 컨텍스트의 입력 포트를 필드로 갖는 것(서비스끼리 부르기)은 지금처럼 금지입니다(ADR 0008).

## 고르지 않은 것

- 서비스가 다른 컨텍스트의 입력 포트를 바로 부르기: 파일은 하나 줄지만, 서비스가 상대의 `...Dto` 와 예외를 알게 되어 상대가 바뀔 때 내 서비스까지 고쳐야 합니다.
- 이벤트를 지금 만들기: 이 봇에는 아직 연쇄 동작이 없어서, 쓰지 않는 발행 코드와 규칙만 늘어납니다.
