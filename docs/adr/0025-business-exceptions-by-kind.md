# 예상한 실패는 공통 부모와 종류로 나누고, 어댑터마다 한 곳에서 처리한다

예외 9개가 모두 `RuntimeException` 을 바로 상속했습니다. 그래서 예외를 하나 더할 때마다 고칠 곳이 넷이었습니다. 예외 클래스, common 의 `ApiErrorCode`, 컨텍스트별 웹 예외 처리기(`SpeechExceptionHandler`, `DeviceExceptionHandler`, `DomainExceptionHandler`)의 메서드, 그 예외가 날 수 있는 디스코드 명령의 `catch` 입니다. 디스코드 명령에는 공통 처리가 없어서, 명령이 잡지 않은 예외가 나면 디스코드는 "애플리케이션이 응답하지 않았어요" 만 보여 줬습니다.

## 결정

- **`shared/domain/BusinessException`**: 사용자에게 알려 줄 수 있는 예상한 실패의 공통 부모입니다. **종류**(`ErrorKind`)와 **오류 코드**(`ApiErrorCode` 값)를 가집니다. HTTP 상태는 가지지 않습니다. 상태를 정하는 건 웹 어댑터의 일입니다.
- 예외가 놓이는 자리(층)는 지금처럼 던지는 쪽이 정합니다.
  - 도메인 규칙 위반: 각 컨텍스트 `domain` 바로 아래 (ADR 0024). 예: `InvalidPairingCodeException`, `UnknownVoiceException`, `shared` 의 `InvalidDomainValueException`
  - 유스케이스 흐름의 실패: `application/exception`. 예: `SpeakerNotInVoiceChannelException`, `NotBotOwnerException`
  - 출력 포트가 약속한 바깥 실패: `application/exception`, 어댑터가 던짐. 예: `SpeechSynthesisException`, `BotCannotJoinVoiceChannelException`
- **종류** (`ErrorKind`)와 HTTP 상태:

  | 종류 | 뜻 | HTTP |
  | --- | --- | --- |
  | `INVALID_INPUT` | 요청 값이 규칙에 맞지 않음 | 400 |
  | `UNAUTHENTICATED` | 누가 보냈는지 모름 | 401 |
  | `FORBIDDEN` | 누군지는 알지만 할 수 없음 | 403 |
  | `CONFLICT` | 지금 상태에서는 할 수 없음 | 409 |
  | `IN_USE` | 다른 사람이 쓰고 있음 | 423 |
  | `EXTERNAL_FAILURE` | 바깥 서비스가 실패함 (로그를 남김) | 502 |

  이미 나간 응답의 상태 코드는 하나도 바뀌지 않습니다. 쓰지 않는 종류(예: 없음 404)는 처음 필요할 때 더합니다.
- **웹**: `shared/adapter/in/web/ApiExceptionHandler` 하나가 종류로 상태를 정하고 `code` 를 실어 보냅니다. 상태를 고르는 `switch` 는 enum 을 빠짐없이 다뤄야 컴파일되므로, 종류를 더하면 여기를 잊을 수 없습니다. 컨텍스트별 처리기 셋은 없앴습니다.
- **디스코드**: `DiscordCommandRegistrar` 가 슬래시 명령마다 `FailureReplyingListener` 로 감싸 등록합니다. 명령이 잡지 않은 `BusinessException` 에는 그 문구로, 그 밖의 예외에는 "처리하다가 문제가 생겼어요. 잠시 뒤에 다시 해 주세요." 로 답하고 로그를 남깁니다. 이미 답한 뒤면 다시 답하지 않습니다. 명령은 문구를 바꾸고 싶은 예외만 직접 잡습니다(`/목소리` 의 없는 목소리, `/허용목록` 의 봇 주인 아님과 서버 ID 형식).
- **코드가 잘못돼 생기는 실패**(불변식이 깨짐, 설정 오류)는 `BusinessException` 이 아니라 `IllegalStateException`, `IllegalArgumentException` 같은 JDK 예외를 그대로 씁니다. 예: `BotSeat` 의 다른 서버 채널, `VoiceCatalog` 의 목록에 없는 기본 목소리. 사용자에게는 일반 안내(웹은 500)가 갑니다.
- 오류 코드는 예외가 `ApiErrorCode` 상수를 직접 씁니다. 도메인이 common 모듈을 알게 되지만, common 은 프레임워크가 없고 오류 코드는 클라이언트와 함께 쓰는 말이라 받아들입니다. 문자열을 예외마다 따로 적고 테스트로 맞추는 것보다 틀릴 곳이 적습니다. 디스코드에서만 생기는 두 예외에도 코드(`unknown-voice`, `not-bot-owner`)를 더했습니다.

## 결과

- 예외를 하나 더할 때 고칠 곳은 예외 클래스와 (새 코드라면) `ApiErrorCode` 둘입니다.
- `ArchitectureTest` 가 확인합니다: 우리가 만든 예외는 모두 `BusinessException` 을 상속한다(`exceptionsAreBusinessExceptions`), `@RestControllerAdvice` 는 `ApiExceptionHandler` 하나뿐이다(`onlyOneWebExceptionHandler`). 인바운드 어댑터가 `ErrorKind` 를 쓸 수 있도록 "애그리거트를 쓰지 않는다" 규칙에서 `domain` 바로 아래 enum 을 뺐습니다.
- 응답 본문 모양(ProblemDetail + `code`, ADR 0013)은 그대로입니다. `ApiResponse` 같은 봉투로 감싸지 않습니다.
- `ApiExceptionHandlerTest` 가 종류마다 HTTP 상태를 고정하고, `FailureReplyingListenerTest` 가 디스코드 공통 답장을 확인합니다.
