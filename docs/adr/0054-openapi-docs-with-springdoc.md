# 웹 API 문서는 springdoc 으로 컨트롤러에서 만든다

- 상태: 채택
- 날짜: 2026-11-01

봇 서버의 웹 API(문장 보내기, 읽힐 음성 채널 묻기, 기기 등록)는 README 와 `libs/api-contract` 의 상수, 요청·응답 record 로만 알 수 있었습니다. 데스크톱 앱 말고 다른 클라이언트(웹 화면, 모바일)를 만들거나 다른 사람이 API 를 보려면 코드를 읽어야 했습니다. 스프링으로 API 를 만드는 팀은 보통 OpenAPI 문서를 코드에서 자동으로 만들고 Swagger UI 로 보여 줍니다. 스프링 부트에서 가장 흔한 도구는 springdoc-openapi 입니다.

## 결정

1. **`springdoc-openapi-starter-webmvc-ui` 를 넣습니다.** OpenAPI JSON 은 `/v3/api-docs`, 화면은 `/swagger-ui.html` 입니다. 경로, 요청·응답 모양은 컨트롤러와 `api-contract` 의 record 에서 읽습니다.
2. **머리말과 인증 방식은 `config/OpenApiConfig` 에 둡니다.** 기기 토큰을 `Authorization: Bearer` 로 싣는 `deviceToken` 을 모든 API 에 걸고, 토큰 없이 부르는 기기 등록에만 `@SecurityRequirements` 로 뺍니다. 오류 응답 모양(ProblemDetail 과 `code`)은 `Problem` 스키마 하나로 적습니다.
3. **컨트롤러에는 `@Operation`, `@ApiResponse`, `@Tag` 만 붙입니다.** 상태 코드마다 어떤 오류 코드(`ApiErrorCode`)가 오는지 적습니다. 요청·응답 record 는 데스크톱 앱과 함께 쓰는 `api-contract` 라서 swagger 어노테이션을 붙이지 않습니다.
4. **문서는 토큰 없이 엽니다.** 경로와 요청 모양만 보여 주고, 문서 화면에서 API 를 부를 때는 기기 토큰이 있어야 합니다. 공개 서버에서 숨기려면 `QUICKTTS_API_DOCS_ENABLED=false` 로 둡니다.

## 결과

- 통합 테스트가 `/v3/api-docs` 를 토큰 없이 받고, 기기 토큰 인증과 기기 등록의 예외가 문서에 적히는지 봅니다.
- swagger-core 가 Jackson 2 를 써서, 봇 서버에 Jackson 3(스프링 부트 4)과 Jackson 2 가 함께 들어갑니다. exe 와 서버 jar 가 몇 MB 커집니다.
- 디스코드 슬래시 명령은 웹 API 가 아니라 문서에 없습니다.

## 고르지 않은 것

- OpenAPI 파일을 손으로 쓰고 코드를 만들기(API 먼저): 클라이언트가 우리 데스크톱 앱 하나이고 같은 저장소에서 `api-contract` 를 함께 쓰므로, 코드에서 문서를 만드는 쪽이 둘이 어긋나지 않습니다.
- Spring REST Docs: 테스트로 문서를 만들어 정확하지만, 테스트마다 문서 조각을 적어야 하고 화면에서 바로 불러 볼 수 없습니다. 스프링 팀에서는 springdoc 이 더 흔합니다.
- 문서도 기기 토큰이 있어야 보기: 문서에 비밀이 없고, 토큰을 받기 전에 기기 등록 방법을 볼 수 있어야 합니다.
