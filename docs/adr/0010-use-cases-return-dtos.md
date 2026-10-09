# 유스케이스는 애그리거트 대신 Dto 를 돌려준다

입력 포트 두 개가 애그리거트를 그대로 돌려줬습니다. `IssuePairingCodeUseCase.issue` 는 `Pairing` 을, `ManageOwnDevicesUseCase.listDevices` 는 `List<Device>` 를 돌려줘서 디스코드 리스너가 애그리거트 안을 다 볼 수 있었습니다. `/연결해제` 리스너는 쓰지도 않는 `Device.tokenHash()` 까지 볼 수 있었고, `/연결` 리스너는 도메인 상수 `Pairing.VALID_FOR` 를 직접 읽었습니다.

지금은 애그리거트에 상태를 바꾸는 메서드가 없어서 문제가 드러나지 않습니다. 하지만 메서드가 생기면 어댑터가 유스케이스를 거치지 않고 도메인을 바꿀 수 있게 되고, 그때는 애그리거트를 쓰는 어댑터가 더 많아져 고칠 곳도 늘어납니다. 고칠 곳이 두 곳일 때 규칙을 정하기로 했습니다.

## 결과

- 입력 포트가 애그리거트를 돌려줘야 할 때는 대신 `port.in.dto` 에 둔 record 를 돌려줍니다. 이름은 `...Dto` 로 끝냅니다. 지금은 `DeviceDto(id, name, registeredAt)` 와 `IssuedPairingCodeDto(code, validFor)` 두 개입니다.
- 애그리거트를 Dto 로 바꾸는 일은 서비스가 Dto 의 `from(...)` 으로 합니다. 변환이 한 줄이라 매퍼 클래스는 두지 않습니다.
- Dto 의 필드는 값 객체(`DeviceId`, `DeviceName`, `PairingCode` 등)를 그대로 씁니다. 값 객체는 바뀌지 않으므로 바깥에 보여도 괜찮습니다. 값 객체만 돌려주는 유스케이스(`Voice`, `VoiceChannelDetails`, `DeviceToken`)는 Dto 로 감싸지 않습니다.
- `port.in` 에는 입력 포트(`...UseCase`), 들어가는 값(`...Command`), 나오는 값(`...Dto`)만 둡니다. 셋은 각각 `usecase` / `command` / `dto` 폴더에 나눠 둡니다 ([ADR 0012](0012-application-subpackages.md)). common 모듈의 웹 DTO 는 `...Request` / `...Response` 로 끝나므로 이름이 겹치지 않습니다. Dto 를 웹 응답으로 바꾸는 일은 컨트롤러가 합니다.
- `ArchitectureTest` 가 두 가지를 확인합니다. 인바운드 어댑터는 애그리거트(각 컨텍스트 `domain` 패키지 바로 아래의 예외가 아닌 클래스)에 의존하지 않고, `port.in.command` 와 `port.in.dto` 의 클래스는 이름이 각각 `Command` 와 `Dto` 로 끝납니다.
