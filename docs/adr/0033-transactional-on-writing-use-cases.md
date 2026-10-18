# DB 에 쓰는 유스케이스에는 @Transactional 을 붙인다

- 상태: 채택
- 날짜: 2026-10-18

ADR 0009 는 "한 유스케이스가 DB 에 두 번 이상 쓸 때만" `@Transactional` 을 붙이게 했고, ADR 0028 은 같은 종류를 한꺼번에 넣을 때 어댑터가 SQL 문장 하나로 처리하게 했습니다. 그래서 서비스 중 `@Transactional` 을 쓰는 곳이 없었고, `JdbcAllowedGuildRepository.saveAll` 은 줄 수만큼 `(:guild0, :allowedAt0), (:guild1, ...)` 를 이어 붙여 `insert` 문장 하나를 만들었습니다.

- 트랜잭션이 필요한지를 유스케이스마다 "DB 에 몇 번 쓰나" 로 따져야 했습니다. 서비스나 저장소가 바뀌어 쓰기가 하나 늘면, 붙여야 하는 줄 모르고 지나치기 쉽습니다.
- 원자성을 SQL 문장 모양으로 지키다 보니, 저장소가 문자열로 SQL 을 만들었습니다. 줄이 아주 많으면 문장 길이와 파라미터 수 한도에도 걸립니다.
- 스프링 프로젝트에서 흔한 모양은 "쓰는 유스케이스(서비스 메서드)가 트랜잭션 경계, 저장소는 그 안에서 평범하게 한 줄씩 쓰기" 입니다. 처음 보는 사람도 이 모양을 기대합니다.

## 결정

1. **DB 에 쓰는 유스케이스 메서드에는 `@Transactional`(`org.springframework.transaction.annotation`)을 붙입니다.** DB 에 쓰는 출력 포트는 `adapter/out/persistence` 의 JdbcClient 저장소로 구현된 포트입니다. `adapter/out/memory` 의 포트(연결 코드, 봇 자리)는 DB 트랜잭션으로 되돌릴 수 없으므로 세지 않습니다. 쓰기가 한 번이어도 붙입니다. 붙일지 따지는 대신 "쓰면 붙인다" 로 정해서 빠뜨리지 않게 합니다.
2. **저장소는 트랜잭션을 열지 않고, 부르는 유스케이스의 트랜잭션 안에서 한 줄씩 씁니다.** 한꺼번에 넣는 `saveAll` 도 줄마다 `insert` 를 하고, 하나라도 실패하면 예외를 던집니다. 이미 넣은 줄은 유스케이스의 트랜잭션이 되돌립니다. SQL 을 문자열로 이어 붙이지 않습니다.
3. **ADR 0028 의 "한 번에 함께 저장하는 것은 애그리거트 하나" 는 그대로입니다.** 트랜잭션이 생겼다고 다른 애그리거트까지 한 트랜잭션에 묶지 않습니다. 같은 종류를 한꺼번에 넣거나 지울 때는 지금처럼 포트 메서드 하나(`saveAll`, `deleteAllByOwner`)로 넘기고, 그 원자성을 이제 트랜잭션이 맡습니다.
4. **트랜잭션 안에서 되돌릴 수 없는 외부 호출(디스코드 서버 나가기, 메시지 보내기, 소리 내기)을 하지 않습니다.** 트랜잭션이 되돌려져도 외부 호출은 남기 때문입니다. 디스코드에서 값을 읽기만 하는 호출(봇이 들어가 있는 서버 목록, 봇 주인)은 괜찮습니다.
5. **DB 에 쓴 결과로 외부 호출을 해야 하면, 쓰는 유스케이스가 도메인 이벤트를 내고 커밋 뒤에 받는 쪽이 외부 호출을 합니다.** 스프링에서 "커밋되면 하기" 의 흔한 모양(`@TransactionalEventListener`, 기본 단계가 커밋 뒤)입니다. 모양은 ADR 0026 의 도메인 이벤트 그대로입니다: 이벤트는 `domain/event` 의 과거형 record, 서비스는 출력 포트(`...EventPort`)로 내고, `ApplicationEventPublisher` 는 그 포트를 구현한 `adapter/out/event` 에만, 받는 쪽은 `adapter/in/event` 에서 입력 포트를 부릅니다. 서비스 안에서 `@Transactional` 메서드를 같은 클래스의 다른 메서드에서 부르면 스프링 프록시를 거치지 않아 트랜잭션이 걸리지 않으므로, 서비스 안에서 이어 부르지 않습니다.
   - 업무 결정이 아니라 시작할 때의 작업 순서(봇 서버가 켜질 때 채우고 나서 나가기)는 시작 리스너가 유스케이스 둘을 차례로 부릅니다.
6. **읽기만 하는 유스케이스에는 붙이지 않습니다(`readOnly = true` 도 쓰지 않습니다).** ADR 0009 에 적은 대로, JPA 를 쓰지 않는 우리에게는 얻는 것이 거의 없습니다.

## 결과

`@Transactional` 을 붙인 유스케이스는 다음과 같습니다.

| 서비스 메서드 | DB 쓰기 | 외부 호출 |
| --- | --- | --- |
| `RegisterDeviceService.register` | 기기 `insert`. 연결 코드를 꺼내는 일은 메모리라서 트랜잭션과 상관없고, 순서는 ADR 0028 그대로 | 없음 |
| `ManageOwnDevicesService.unlink`, `unlinkAll` | 기기 `delete` | 없음 |
| `ManageVoiceService.changeVoice` | 목소리 설정 `insert ... on duplicate key update` | 없음 (목소리 목록은 메모리 값) |
| `ManageGuildAllowlistService.allow`, `disallow` | 허용 목록 `insert`, `delete` | 없음 (빼면 이벤트만 내고, 나가기는 커밋 뒤) |
| `EnforceGuildAllowlistService.fillIfEmpty` | 허용 목록 처음 채우기 `saveAll` | 없음 (서버 목록 읽기만) |

디스코드 서버 나가기가 섞여 있던 두 곳은 5번대로 나눴습니다.

- 봇 서버가 켜질 때: 예전 `leaveAllNotAllowed` 는 허용 목록이 비어 있으면 채우고, 아니면 허용되지 않은 서버에서 나갔습니다. 채우기를 `EnforceGuildAllowlistUseCase.fillIfEmpty`(트랜잭션)로 떼고, `GuildJoinListener` 가 `fillIfEmpty` 다음에 `leaveAllNotAllowed`(트랜잭션 없음)를 부릅니다. `leaveAllNotAllowed` 는 허용 목록이 아직 비어 있으면 아무 데서도 나가지 않아서, 채우기가 실패했거나 순서가 바뀌어도 봇 주인의 서버에서 나가지 않습니다.
- `/허용목록 삭제`: 예전 `disallow` 는 목록에서 빼고 이어서 그 서버에서 나갔습니다. 이제 `disallow` 는 트랜잭션 안에서 빼고, 실제로 뺐으면 `GuildDisallowed` 이벤트를 `GuildAllowlistEventPort` 로 냅니다. 커밋되면 `guild/adapter/in/event/GuildDisallowedListener` 가 받아 `EnforceGuildAllowlistUseCase.leaveIfNotAllowed` 를 부릅니다. 봇이 초대됐을 때 쓰는 것과 같은 유스케이스라서 나갈지 정하는 규칙도 한 곳에 있습니다. 봇 주인이 아니거나 빼기가 되돌려지면 이벤트를 받지 않아서 나가지 않습니다. 디스코드 명령(`AllowlistCommandListener`)은 `disallow` 하나만 부르므로, 다른 입구가 생겨도 순서를 다시 맞출 필요가 없습니다.

그 밖에:

- `JdbcAllowedGuildRepository.saveAll` 은 줄마다 `insert` 를 합니다. 저장소 테스트(`JdbcAllowedGuildRepositoryTest`)는 유스케이스 대신 `TransactionTemplate` 으로 트랜잭션을 열고, 중간에 실패하면 하나도 남지 않는지 H2 와 MySQL 양쪽에서 봅니다.
- `GuildAllowlistTransactionTest` 를 두었습니다. 스프링 문맥을 작게 띄워, 처음 채우다 세 번째 줄에서 실패하면 앞의 두 줄도 되돌려지는지, 시작할 때 디스코드 서버 나가기가 트랜잭션 밖에서 일어나는지, 허용 목록에서 빼면 커밋된 뒤에(다른 연결에서도 빠진 것이 보일 때) 나가는지를 봅니다. 다른 서비스 테스트는 지금처럼 스프링 없이 `new` 로 만듭니다.
- 허용 목록 `saveIfAbsent` 는 중복 키 오류를 저장소 안에서 잡아 `false` 를 돌려줍니다. H2 와 MySQL 은 문장 하나가 실패해도 트랜잭션이 계속 쓸 수 있는 상태로 남아서, 트랜잭션 안에서도 그대로 동작합니다.
- ADR 0009 의 붙이는 기준과 ADR 0028 의 2번, 결과를 이 ADR 에 맞게 고쳤습니다.
- 어느 메서드에 붙였는지는 ArchUnit 으로 잡기 어려워서 리뷰로 지킵니다.

## 고르지 않은 것

- 지금처럼 SQL 문장 하나로 원자성 지키기: 트랜잭션 없이 되지만, 저장소가 SQL 을 문자열로 만들고, 쓰기가 늘 때마다 붙일지 다시 따져야 합니다.
- 저장소 메서드(`saveAll`)에 `@Transactional` 붙이기: 저장소 하나 안에서는 묶이지만, 유스케이스가 저장소를 두 번 부르면 따로 커밋됩니다. 트랜잭션 경계는 유스케이스 단위가 맞습니다.
- 서비스 클래스 전체에 `@Transactional` 붙이기: 읽기 메서드와, 디스코드 서버에서 나가는 메서드까지 트랜잭션 안에 들어갑니다.
- 한 메서드 안에서 쓰기만 트랜잭션으로 감싸기(`TransactionTemplate`, 자기 자신을 주입받아 부르기): `application` 은 스프링 중 `@Service`, `@Transactional` 만 쓰고(ADR 0009), 자기 자신을 주입받는 모양은 처음 보는 사람이 프록시를 알아야 읽힙니다.
- `/허용목록 삭제` 에서 디스코드 명령이 `disallow` 다음에 `leaveIfNotAllowed` 를 차례로 부르기: 파일이 넷 덜 생기지만, "빼고 나서 나간다" 는 업무 순서가 디스코드 어댑터에 들어가서 다른 입구(예: 웹)가 생기면 순서를 거기서 또 맞춰야 합니다.
- 읽기 유스케이스에 `readOnly = true` 붙이기: 6번과 같은 이유로 쓰지 않습니다.
