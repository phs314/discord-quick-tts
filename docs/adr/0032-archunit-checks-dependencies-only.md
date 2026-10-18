# ArchUnit 은 의존 방향만 검사한다

`ArchitectureTest` 에는 규칙이 29개 있었고, `TestDoubleArchitectureTest` 가 테스트 코드 규칙을 하나 더 봤습니다. 이 가운데 어느 패키지가 어느 패키지를 알아도 되는지 보는 의존 규칙은 12개뿐이고, 나머지 17개와 테스트 대역 규칙은 이름 끝(`...Port`, `...UseCase`, `...Command`), 파일 자리(`exception` 폴더, `domain` 바로 아래), 클래스 모양(입력 포트 하나만 구현, 메서드 인자 하나)을 봤습니다.

- 의존 방향은 한 번 어기면 되돌리기 어렵고, 코드만 봐서는 import 한 줄이 경계를 넘는지 알아채기 어렵습니다. 기계가 보는 것이 맞습니다.
- 이름과 자리 규칙은 리뷰에서 바로 보입니다. 그런데 테스트로 막으니 규칙을 조금 다듬을 때마다(예: 어댑터 이름, 필드 이름) 테스트 코드도 함께 고쳐야 했고, 예외를 둘 자리가 생기면 테스트에 조건을 더해야 했습니다. 테스트가 길어지면서 무엇을 막는 테스트인지 한눈에 보이지 않게 됐습니다.

## 결정

1. **`ArchitectureTest` 는 의존 방향만 검사합니다.** ArchUnit 을 가장 흔히 쓰는 방식입니다. 남기는 규칙은 12개입니다.
   - 계층: `speechIsHexagonal`, `deviceIsHexagonal`, `guildIsHexagonal`
   - 컨텍스트 사이: `everyContextIsOnTheContextMap`, `contextsMeetOnlyAtPublicEntrances`, `sharedKernelDependsOnNoContext` (ADR 0002, 0026)
   - 어댑터: `inboundAdaptersUseOnlyInputPorts`, `inboundAdaptersDoNotUseAggregates` (ADR 0010), `adaptersDoNotUseConfig` (ADR 0023)
   - 애플리케이션과 도메인: `servicesDoNotUseOtherUseCases` (ADR 0008), `domainIsFrameworkFree`, `applicationUsesOnlySpringAnnotations` (ADR 0009)
2. **이름과 자리 규칙은 ADR 에 그대로 두고 리뷰로 지킵니다.** 지운 규칙은 17개입니다.
   - 이름: `outputPortsAreInterfacesNamedPort`, `outboundAdaptersAreNamedAfterTheirPort`, `useCasesAreInterfacesNamedUseCase`, `commandsAreNamedCommand`, `queriesAreNamedQuery`, `dtosAreNamedDto`, `portFieldsAreNamedAfterTheirType` (ADR 0004, 0010, 0012, 0027, 0029)
   - 자리: `inputPortPackageHasOnlySubpackages`, `applicationExceptionsLiveInExceptionPackage`, `exceptionPackageHoldsOnlyExceptions`, `domainExceptionsLiveNextToAggregates`, `webMappersLiveNextToTheirController` (ADR 0012, 0024, 0030)
   - 모양: `useCaseMethodsTakeOneCommandOrQuery`, `eachServiceImplementsExactlyOneUseCase`, `controllersLeaveConversionToWebMappers`, `exceptionsAreBusinessExceptions`, `onlyOneWebExceptionHandler` (ADR 0008, 0025, 0029, 0030)
3. 테스트 코드의 대역 규칙(`Fake<포트 이름>` 을 포트와 같은 패키지에 둔다, ADR 0027)도 리뷰로 지키고 `TestDoubleArchitectureTest` 를 지웁니다.

## 결과

- ADR 0004, 0008, 0010, 0012, 0024, 0025, 0027, 0029, 0030 의 이름·자리 규칙은 바뀌지 않습니다. 각 ADR 에서 "`ArchitectureTest` 가 확인합니다" 라고 적은 곳만 "리뷰로 지킵니다" 로 고쳤습니다.
- `ArchitectureTest` 가 400줄에서 200줄 정도로 줄고, 규칙 이름만 읽어도 모두 "무엇이 무엇을 모른다" 는 뜻입니다.
- README 의 규칙 목록을 빌드가 확인하는 의존 규칙과 리뷰로 지키는 약속으로 나눴습니다.
- 이름이나 자리를 어긴 코드가 빌드를 통과할 수 있습니다. 리뷰에서 ADR 을 보고 짚습니다.

## 고르지 않은 것

- 모두 그대로 두기: 규칙을 다듬을 때마다 테스트도 함께 고쳐야 하고, 의존 규칙이 이름 규칙 사이에 묻혀 잘 보이지 않습니다.
- 이름 규칙만 다른 테스트 클래스로 옮기기: 파일은 나뉘지만 고칠 테스트의 양은 같습니다.
- 정적 분석 도구(Checkstyle 이름 규칙 등)로 옮기기: 패키지 자리마다 다른 접미사를 거는 일은 ArchUnit 보다 쓰기 어렵고, 도구가 하나 더 늘어납니다.
