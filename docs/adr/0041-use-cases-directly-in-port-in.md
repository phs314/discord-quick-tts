# 입력 포트(...UseCase)는 port/in 바로 아래에 둔다

- 상태: 채택
- 날짜: 2026-10-19

ADR 0012 는 `port/in` 을 `usecase` / `command` / `dto` 폴더로 나누고 `port/in` 바로 아래에는 클래스를 두지 않기로 했고, ADR 0029 가 `query` 를 더했습니다. 그런데 `port/in` 이 이미 "입력 포트" 라는 뜻이라, 입력 포트인 `...UseCase` 를 다시 `usecase` 폴더에 넣으면 같은 말을 두 번 쓰게 됩니다. `port/in` 을 열면 폴더만 넷 보이고, 이 컨텍스트가 무슨 일을 하는지(유스케이스 목록)는 한 번 더 들어가야 보였습니다. 헥사고날 구조의 스프링 프로젝트는 유스케이스 인터페이스를 `port/in` 에 바로 두는 경우가 더 흔합니다. 이 ADR 은 ADR 0004 표의 입력 포트 자리, ADR 0010 과 ADR 0012 의 `usecase` 폴더를 바꿉니다.

## 결정

1. **입력 포트(`...UseCase`)는 `application/port/in` 바로 아래에 둡니다.** `usecase` 폴더는 없앱니다.
2. 유스케이스로 들어가고 나오는 값은 지금처럼 하위 폴더에 둡니다: `port/in/command`(`...Command`), `port/in/query`(`...Query`), `port/in/dto`(`...Dto`).
3. `port/in` 바로 아래에는 `UseCase` 로 끝나는 인터페이스만 둡니다. 이름과 자리 규칙이라 리뷰로 지킵니다(ADR 0032).

## 결과

- 세 컨텍스트의 입력 포트 11개가 자리를 옮겼습니다. 이름, 메서드, 동작은 바뀌지 않고 패키지와 import 만 바뀝니다.

  지금:

  ```
  application/port/in/
  ├── usecase/    SpeakQuickChatUseCase, ManageVoiceUseCase, ...
  ├── command/    SpeakQuickChatCommand, ...
  ├── query/      FindMyVoiceChannelQuery, ...
  └── dto/        DeviceDto, ...
  ```

  바꾼 뒤:

  ```
  application/port/in/
  ├── SpeakQuickChatUseCase, ManageVoiceUseCase, ...
  ├── command/    SpeakQuickChatCommand, ...
  ├── query/      FindMyVoiceChannelQuery, ...
  └── dto/        DeviceDto, ...
  ```

- `ArchitectureTest` 의 `servicesDoNotUseOtherUseCases` 는 서비스 필드가 `application.port.in.usecase` 대신 `application.port.in` 패키지(하위 폴더는 빼고)의 타입이 아닌지 봅니다.
- 다른 컨텍스트의 공개 입구는 지금처럼 `application.port.in` 과 그 하위 폴더 전체입니다(ADR 0026).
- 옛 ADR 본문의 `port/in/usecase` 는 ADR 0034 에 따라 고치지 않습니다. 지금 자리는 저장소 README 를 봅니다.

## 고르지 않은 것

- 지금처럼 `usecase` 폴더 두기: 입력 포트, Command, Query, Dto 가 모두 폴더로 나뉘어 모양은 고르지만, `port/in/usecase` 는 같은 뜻을 두 번 쓰고 유스케이스 목록이 한 단계 더 깊이 숨습니다.
- `command`, `query`, `dto` 도 없애고 `port/in` 한 폴더에 모두 두기: 폴더가 가장 얕아지지만, 컨텍스트마다 파일이 8~12개 섞여 ADR 0012 이전의 문제(무엇이 입구이고 무엇이 값인지 한눈에 안 보임)로 돌아갑니다.
