# application 의 유스케이스, Command, Dto, 예외를 폴더로 나눈다

`port/in` 한 폴더에 입력 포트(`...UseCase`), 들어가는 값(`...Command`), 나오는 값(`...Dto`)이 섞여 있었고, 애플리케이션 예외는 `application` 바로 아래에 `port`, `service` 폴더와 나란히 놓여 있었습니다. 이름 끝(ADR 0004, 0010)으로 구분은 됐지만, 폴더를 열었을 때 한눈에 나뉘어 보이지 않았고 새 파일을 어디 둘지도 이름 규칙을 떠올려야 알 수 있었습니다.

## 결과

각 컨텍스트(`speech`, `device`)의 `application` 안을 이렇게 나눕니다.

```
application/
├── exception/          애플리케이션 예외
├── port/in/usecase/    입력 포트 (...UseCase)
├── port/in/command/    유스케이스로 들어가는 값 (...Command)
├── port/in/dto/        유스케이스가 돌려주는 값 (...Dto)
├── port/out/           출력 포트 (...Port)
└── service/            유스케이스 구현 (...Service)
```

- 폴더는 각 컨텍스트 안에 둡니다. 컨텍스트를 가로지르는 최상위 `exception` 패키지는 두지 않습니다(ADR 0002).
- 예외는 `port/in` 이 아니라 `application/exception` 에 둡니다. 서비스뿐 아니라 TTS 출력 어댑터도 `SpeechSynthesisException` 을 던지기 때문입니다.
- 지금 파일이 없는 폴더(speech 의 `dto`, device 의 `command`)는 만들지 않고, 처음 필요할 때 만듭니다.
- `device` 의 공개 입구는 여전히 `device.application.port.in` 과 그 하위 폴더 전체입니다.
- `ArchitectureTest` 가 폴더 규칙을 확인합니다. `port.in` 바로 아래에는 클래스를 둘 수 없고, `usecase` 에는 `UseCase` 로 끝나는 인터페이스만, `command` 에는 `Command`, `dto` 에는 `Dto` 로 끝나는 클래스만 둡니다. `application` 의 예외는 `exception` 에만 있어야 하고, `exception` 에는 예외만 둡니다.
