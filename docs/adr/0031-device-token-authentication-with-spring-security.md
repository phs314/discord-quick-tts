# 기기 토큰 인증은 스프링 시큐리티 필터가 맡는다

- 상태: 채택
- 날짜: 2026-10-18

quick chat 컨트롤러가 요청마다 `Authorization` 헤더를 직접 읽고, `device` 의 `AuthenticateDeviceUseCase` 에 기기 토큰의 주인을 물었습니다(ADR 0005). 엔드포인트가 둘뿐이라 동작에는 문제가 없었지만, 두 가지가 마음에 걸렸습니다.

- 인증이 컨트롤러 메서드마다 한 줄씩 들어갑니다. 새 엔드포인트에서 그 줄을 빠뜨리면 토큰 없이 열린 API 가 되는데, 빌드도 테스트도 알려 주지 않습니다.
- 스프링에서 가장 흔한 방식은 스프링 시큐리티입니다. 이 프로젝트를 다른 프로젝트의 출발점으로 쓰려면 처음 보는 사람이 바로 알아보는 모양이어야 합니다.

## 결정

1. **스프링 시큐리티(`spring-boot-starter-security`)를 씁니다.** 보안 규칙은 `config/SecurityConfig` 의 `SecurityFilterChain` 한 곳에 있습니다.
   - 기기 등록(`POST /api/v1/devices`)만 토큰 없이 열고, 나머지 경로는 모두 인증이 있어야 합니다. 새 엔드포인트는 따로 적지 않아도 막힙니다.
   - 클라이언트는 매 요청에 토큰을 싣는 프로그램이라 세션(`STATELESS`), 로그인 화면, HTTP Basic, 로그아웃, CSRF 토큰을 끕니다.
   - 아이디·비밀번호 사용자가 없으므로 스프링 부트가 만드는 기본 사용자(`UserDetailsServiceAutoConfiguration`)는 뺍니다.
2. **기기 토큰을 읽는 필터(`DeviceTokenAuthenticationFilter`, `OncePerRequestFilter`)는 `device/adapter/in/web` 에 둡니다.** 기기 토큰은 device 컨텍스트의 것이라, 토큰을 읽고 주인을 찾는 일도 device 의 인바운드 어댑터가 합니다.
   - `Bearer <토큰>` 이 맞으면 주인(`DiscordUserId`)을 principal 로 담은 `UsernamePasswordAuthenticationToken` 을 `SecurityContextHolder` 에 싣습니다.
   - 토큰이 없거나 틀리면 아무것도 싣지 않고 다음 필터로 넘깁니다. 막는 일은 1번의 경로 규칙이 합니다.
3. **컨트롤러는 `@AuthenticationPrincipal DiscordUserId speaker` 로 보낸 사람을 받습니다.** 헤더도 기기 토큰도 모릅니다.
4. **401 응답 모양은 그대로입니다.** 인증 없이 막힌 요청은 `DeviceTokenAuthenticationEntryPoint` 가 스프링 MVC 의 예외 처리(`HandlerExceptionResolver`)에 `DeviceUnauthorizedException` 을 넘겨서, 다른 실패와 같은 ProblemDetail 에 `code: device-unauthorized` 를 싣습니다(ADR 0025). 클라이언트는 바뀌지 않습니다.

## 결과

- `speech` 가 `device` 를 부르던 유일한 길이 없어졌습니다. 컨텍스트 지도(ADR 0026)는 모든 컨텍스트가 다른 컨텍스트를 부르지 않는 상태가 되고, 지도와 규칙은 다음 접점을 위해 그대로 둡니다.
- `QuickChatController` 는 유스케이스 둘만 갖고, `QuickChatWebMapper.toQuery(String)` 은 지웠습니다. 웹 매퍼(ADR 0030)는 그대로입니다.
- 토큰을 읽는 경우의 수(헤더 없음, `Bearer` 아님, 빈 토큰, 해제된 기기, 맞는 토큰)는 필터 테스트가, 401 응답 모양은 통합 테스트가 봅니다. 컨트롤러 테스트는 보안 설정을 함께 올리므로(ADR 0017) 토큰 없이 막히는 경우도 401 과 `device-unauthorized` 로 확인합니다.
- 토큰 없이 없는 경로를 부르면 404 대신 401 입니다. 막힌 경로인지 없는 경로인지 알려 주지 않는 스프링 시큐리티 기본 동작입니다.
- 응답에 스프링 시큐리티 기본 보안 헤더(`X-Content-Type-Options`, `Cache-Control` 등)가 붙습니다.

## 고르지 않은 것

- 인증을 컨트롤러에 그대로 두기: 엔드포인트를 늘릴 때 빠뜨리기 쉽고, 흔한 모양이 아닙니다.
- `HandlerMethodArgumentResolver` 와 직접 만든 `@AuthenticatedUser` 어노테이션: 의존이 늘지 않고 가볍지만, 파라미터를 빠뜨린 엔드포인트는 그대로 열리고 우리만 아는 방식입니다.
- `HandlerInterceptor`: 경로로 막을 수는 있지만 보낸 사람을 컨트롤러에 넘기는 길(요청 속성)을 따로 정해야 합니다.
- 필터를 `@Component` 로 두기: 스프링 부트가 서블릿 필터로도 한 번 더 등록합니다. 보안 설정에서 직접 만들어 보안 필터 체인에만 넣습니다.
- 엔트리 포인트가 JSON 을 직접 쓰기: 오류 응답을 만드는 곳이 둘이 됩니다.
- OAuth2 리소스 서버(JWT): 토큰을 발급하는 인증 서버가 없고, 기기 토큰은 해제하면 바로 막혀야 해서 DB 조회가 맞습니다.
