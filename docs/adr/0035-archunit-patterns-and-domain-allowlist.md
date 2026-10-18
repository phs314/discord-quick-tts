# 도메인과 애플리케이션이 어댑터를 모르는지는 폴더 이름과 상관없이 검사하고, 도메인은 써도 되는 것만 적는다

- 상태: 채택
- 날짜: 2026-10-18

ADR 0032 가 남긴 의존 규칙 12개에 빈틈이 둘 있었습니다. 이 ADR 은 ADR 0032 의 규칙 목록 가운데 `domainIsFrameworkFree` 를 바꾸고 규칙 하나를 더합니다.

- 계층 규칙(`speechIsHexagonal` 등)은 어댑터 폴더를 `.adapter("tts", "..bot.speech.adapter.out.tts..")` 처럼 하나씩 적고 `withOptionalLayers(true)` 입니다. 그래서 `adapter/out/cache` 처럼 어댑터 폴더를 새로 만들고 규칙에 적지 않으면, 도메인이 그 폴더를 써도 빌드가 통과합니다. 새 컨텍스트는 `everyContextIsOnTheContextMap` 이 막지만 새 어댑터 폴더는 막는 규칙이 없었습니다.
- `domainIsFrameworkFree` 는 쓰면 안 되는 것(스프링, JDA, LavaPlayer, jdave, config)을 적은 목록이었습니다. 목록에 없는 라이브러리(`jakarta..`, `tools.jackson..`, `org.slf4j..` 등)는 도메인이 써도 통과했습니다.

## 결정

1. **도메인과 애플리케이션은 어떤 어댑터 폴더도 모릅니다.** `domainAndApplicationDoNotKnowAdapters` 가 `..bot.*.adapter..` 패턴으로 검사합니다. 계층 규칙은 어댑터끼리 서로 모르는지를 보는 데 그대로 둡니다.
2. **도메인은 써도 되는 것만 적습니다.** `domainIsFrameworkFree` 대신 `domainDependsOnlyOnJavaAndDomain` 이 "자바 표준 라이브러리(`java..`), 도메인(`..bot.*.domain..`), 클라이언트와 함께 쓰는 API 약속(`common`)" 만 허용합니다. `common` 은 도메인 예외가 오류 코드를 직접 들고 있어서(ADR 0025) 들어갑니다.

## 결과

- 규칙 수는 13개입니다. 둘 다 "무엇이 무엇을 모른다" 는 의존 규칙이라 ADR 0032 의 범위 안입니다.
- 어댑터 폴더를 새로 만들 때 계층 규칙에 적는 것을 잊어도, 도메인과 애플리케이션이 그 폴더를 쓰면 빌드가 실패합니다.
- 도메인에서 새 라이브러리를 쓰려면 이 규칙의 허용 목록을 고쳐야 하므로, 그 의존이 리뷰에서 드러납니다.
- 애플리케이션 쪽 `applicationUsesOnlySpringAnnotations` 는 지금처럼 금지 목록입니다. 애플리케이션은 스프링 애너테이션 두 개를 쓰므로 허용 목록으로 바꾸면 적을 것이 많아지고, 어댑터를 모르는지는 1번이 봅니다.

## 고르지 않은 것

- 계층 규칙에 어댑터 폴더를 빠짐없이 적는지 검사하는 규칙: 폴더 이름을 보는 규칙이라 ADR 0032 가 뺀 이름·자리 검사에 가깝습니다.
- `layeredArchitecture()` 로 계층 규칙을 패턴 하나로 다시 쓰기: 어댑터끼리 서로 모르는지를 보려면 결국 어댑터를 나눠 적어야 해서 지금 계층 규칙과 같아집니다.
