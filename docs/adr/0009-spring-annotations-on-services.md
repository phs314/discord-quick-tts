# 서비스에는 @Service, @RequiredArgsConstructor 를 붙이고 @Transactional 은 DB 에 쓰는 유스케이스에 쓴다

ADR 0001 에서는 `application` 이 스프링을 전혀 모르게 하고, 서비스는 `config/UseCaseConfig` 의 `@Bean` 메서드로 등록했습니다. ADR 0008 로 서비스가 7개가 되자 유스케이스를 하나 만들 때마다 서비스, 생성자, 빈 메서드를 함께 써야 했습니다. 스프링 프로젝트에서 흔한 모양(`@Service` + `@RequiredArgsConstructor`)이 더 읽기 쉽다고 보고 바꾸기로 했습니다.

## 결과

- 서비스에는 `@Service` 와 Lombok `@RequiredArgsConstructor` 를 붙이고 생성자를 직접 쓰지 않습니다. 컴포넌트 스캔이 서비스를 등록하므로 `UseCaseConfig` 는 없애고, 남은 `Clock` 빈은 `ClockConfig` 로 옮겼습니다.
- `application` 은 스프링 중 `org.springframework.stereotype`(`@Service`)와 `org.springframework.transaction.annotation`(`@Transactional`)만 쓸 수 있습니다. 나머지 스프링, JDA, LavaPlayer 는 여전히 모릅니다. `domain` 은 계속 스프링을 전혀 모릅니다. `ArchitectureTest` 가 둘 다 확인합니다.
- ~~Lombok 은 서비스의 생성자에만 씁니다.~~ ADR 0011 로 필드 주입만 하는 모든 스프링 빈으로 넓혔습니다. 값 객체(record)와 애그리거트(POJO class, ADR 0003)는 그대로 직접 씁니다.
- 서비스 테스트는 지금처럼 스프링 없이 `new` 로 만들어 가짜 포트를 넣어 돌립니다. Lombok 이 만든 생성자도 필드 순서대로 인자를 받습니다.

## @Transactional 을 붙이는 기준

~~한 유스케이스가 DB 에 두 번 이상 쓰고, 그 쓰기들이 함께 성공하거나 함께 실패해야 할 때 그 서비스 메서드에 `@Transactional` 을 붙입니다.~~ ADR 0033 으로 DB 에 쓰는 유스케이스 메서드에는 쓰기가 한 번이어도 `@Transactional` 을 붙입니다. 읽기만 하는 유스케이스에는 붙이지 않습니다. `readOnly = true` 는 JPA 의 플러시를 줄이는 것이 주된 이득인데, 우리는 JdbcClient 와 H2 라서 얻는 것이 거의 없습니다.

2026-10-09 에 유스케이스 7개를 모두 확인했고, 그때 기준으로는 붙일 곳이 없었습니다. 아래 표는 그때의 판단이고, 지금 붙인 곳은 ADR 0033 에 있습니다.

| 유스케이스 | DB 쓰기 | 판단 |
| --- | --- | --- |
| 연결 코드 발급 | 없음 (연결 코드는 메모리에만 둠) | 필요 없음 |
| 기기 등록 | `insert` 한 번. 연결 코드를 꺼내는 일은 메모리라서 DB 트랜잭션으로 되돌릴 수 없음 | 필요 없음 |
| 기기 인증 | 읽기만 | 필요 없음 |
| 내 기기 관리 | 목록은 읽기, 해제와 모두 해제는 `delete` 한 번씩 | 필요 없음 |
| 목소리 관리 | 바꾸기는 `merge` 한 번 | 필요 없음 |
| quick chat 읽어 주기 | 없음 (목소리 설정만 읽음) | 필요 없음 |
| 내 음성 채널 찾기 | 없음 | 필요 없음 |

~~문장 하나는 DB 가 스스로 원자적으로 처리하므로, 지금은 트랜잭션으로 더 묶을 것이 없습니다.~~ ~~"내 데이터 지우기"(기기와 목소리 설정을 함께 지우기)처럼 여러 테이블에 쓰는 유스케이스가 처음 생기면 그 서비스에 붙입니다.~~ ADR 0028 로 기기와 목소리 설정은 서로 다른 애그리거트(다른 컨텍스트)라 한 트랜잭션에 묶지 않고 이벤트로 잇습니다. ~~`@Transactional` 은 애그리거트 하나를 여러 테이블에 나눠 저장할 때(애그리거트 안의 목록 등) 붙입니다.~~ ADR 0033 으로 DB 에 쓰는 유스케이스에는 모두 붙입니다.

~~처음 붙인 곳은 허용 목록 지키기(ADR 0015)의 `EnforceGuildAllowlistService.leaveAllNotAllowed` 입니다. 허용 목록이 비어 있으면 봇이 들어가 있는 디스코드 서버를 하나씩 `insert` 하는데, 중간에 실패해 일부만 남으면 다음에 켤 때 목록이 비어 있지 않다고 보고 나머지 디스코드 서버에서 나가 버립니다. 그 서비스만 쓰는 테스트(`EnforceGuildAllowlistTransactionTest`)는 다른 서비스 테스트와 달리 스프링 문맥을 작게 띄워 트랜잭션이 실제로 되돌려지는지 봅니다.~~ ~~ADR 0028 로 한꺼번에 넣기를 저장소의 `saveAll`(SQL 문장 하나)로 옮겨서, 지금은 `@Transactional` 을 쓰는 서비스가 없습니다.~~ ADR 0033 으로 `saveAll` 은 한 줄씩 넣고, 처음 채우기(`EnforceGuildAllowlistService.fillIfEmpty`)의 `@Transactional` 이 모두 되돌립니다. `GuildAllowlistTransactionTest` 도 다시 두었습니다(처음 이름은 `EnforceGuildAllowlistTransactionTest`).

2026-10-17 에 ADR 0028 과 함께 기준을 더했습니다.

- 같은 종류를 한꺼번에 넣거나 지우는 일은 서비스에서 묶지 않고 포트 메서드 하나(`saveAll`, `deleteAllByOwner`)로 넘깁니다. ~~원자성은 어댑터의 SQL 문장 하나가 지킵니다.~~ ADR 0033 으로 원자성은 유스케이스의 트랜잭션이 지킵니다.
- 읽은 값을 보고 판단해서 쓰는 경우는 `@Transactional` 만으로 막을 수 없습니다. 두 요청이 같은 값을 읽기 때문입니다. 고쳐 쓰는 애그리거트는 `version` 으로 저장하거나(ADR 0028) 조건을 SQL 에 넣습니다(`update ... where 남은_수량 > 0`).
- 트랜잭션 안에서 되돌릴 수 없는 외부 호출(디스코드 메시지, 서버 나가기, TTS)을 하지 않습니다. 트랜잭션이 되돌려져도 외부 호출은 남습니다. 필요하면 커밋 뒤에 합니다. ADR 0033 부터는 유스케이스 메서드를 둘로 나누고 인바운드 어댑터가 쓰기 → 외부 호출 순서로 부릅니다.
