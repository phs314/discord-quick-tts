# 저장소 어댑터는 JPA 엔티티를 도메인과 따로 두고 매퍼로 옮긴다

- 상태: 채택
- 날짜: 2026-10-18

저장소 어댑터 3개(`device`, `voice_preference`, `allowed_guild`)는 `JdbcClient` 로 SQL 을 직접 쓰고, 읽은 행을 그 자리에서 애그리거트로 만들었습니다(ADR 0016, ADR 0030 6번). 테이블 3개에 관계가 없어서 이것으로 충분했지만, 스프링 프로젝트의 저장소는 대부분 Spring Data JPA 로 짜여 있어서 처음 보는 사람에게는 낯선 모양이었습니다. 이 저장소를 다른 프로젝트의 출발점으로도 쓰려면, 도메인은 지금처럼 프레임워크를 모르게 두면서(ADR 0035) 저장 쪽은 흔한 모양으로 맞추는 편이 낫습니다. 이 ADR 은 ADR 0016 의 "JPA 로 바꾸지 않는다" 와 ADR 0030 6번의 "DB 행 → 애그리거트는 저장소 어댑터 안에서" 를 바꿉니다.

## 결정

1. **저장은 Spring Data JPA 로 합니다.** 컨텍스트의 `adapter/out/persistence` 에는 저장하는 것마다 네 파일을 둡니다. 어댑터 말고는 모두 패키지 밖에 공개하지 않습니다.
   - `<이름>JpaEntity`: 테이블의 한 줄입니다. 열 이름은 `@Column` 으로 모두 적습니다.
   - `SpringData<이름>Repository`: `JpaRepository` 를 상속하는 인터페이스입니다. 같은 패키지의 어댑터만 씁니다.
   - `<이름>PersistenceMapper`: 엔티티와 도메인을 서로 바꾸는 `final` 클래스이고, `static` 메서드 `toEntity`, `toDomain` 만 둡니다. 웹 매퍼(ADR 0030)와 같은 모양입니다.
   - `Jpa<이름>Repository`: 출력 포트 구현입니다. ADR 0027 4번 이름 규칙 그대로이고 기술 이름만 `Jdbc` 에서 `Jpa` 로 바뀝니다.
2. **도메인 클래스에는 JPA 애너테이션을 붙이지 않습니다.** 애그리거트와 값 객체는 지금 그대로이고, `ArchitectureTest` 의 도메인 규칙도 그대로 통과합니다.
3. **스키마는 계속 Flyway 가 만듭니다.** JPA 는 켜질 때 엔티티가 테이블과 맞는지만 확인합니다(`ddl-auto: validate`). 웹 요청이 끝날 때까지 DB 연결을 붙잡지 않도록 `open-in-view` 는 끕니다.
4. **기본 키를 도메인이 정하고 새로 넣기만 하는 엔티티**(`device`, `allowed_guild`)는 `Persistable` 로 새 줄인지 알려서 바로 `insert` 합니다. 같은 키가 있으면 덮어쓰지 않고 실패하므로, `AllowedGuildPort.saveAll` 의 "하나라도 넣지 못하면 예외" 약속이 그대로 지켜집니다. 사용자마다 한 줄을 고쳐 쓰는 `voice_preference` 는 `save` 가 있으면 고치고 없으면 넣습니다.
5. **지우는 메서드는 JPQL `delete` 한 문장**(`@Modifying @Query`)으로 지운 줄 수를 돌려받습니다. Spring Data 의 기본 메서드처럼 혼자 불려도 되도록 저장소 인터페이스의 그 메서드에 `@Transactional` 을 붙입니다. 유스케이스의 트랜잭션(ADR 0033)이 있으면 그 안에서 돕니다.
6. `Instant` 는 `hibernate.type.preferred_instant_jdbc_type: TIMESTAMP` 로 시간대 없는 `datetime(6)` 열에 `JdbcClient` 때와 같은 값으로 넣고 읽습니다.

## 결과

- 테이블, 마이그레이션, 저장된 데이터는 바뀌지 않습니다. Flyway 전에 만든 H2 파일도 그대로 열립니다(`ExistingH2DatabaseMigrationTest`).
- 저장소 테스트는 지금처럼 H2 와 MySQL 양쪽에서 돕니다(ADR 0016). 테스트의 `support/TestJpa` 가 서버와 같은 JPA 설정으로 작은 스프링 문맥을 띄웁니다. JPA 설정이 `application.yml` 과 `TestJpa` 두 곳에 있으니 함께 고칩니다.
- 서버 jar 가 약 29MB 커지고(Hibernate, Spring Data. 93MB → 122MB), exe 도 그만큼 커집니다. 켜질 때 Hibernate 를 준비하는 시간도 더해집니다.
- 목소리 저장은 SQL 한 문장(`insert ... on duplicate key update`) 대신 읽고 나서 넣거나 고칩니다. 같은 사용자가 처음 목소리를 고르는 요청 둘이 동시에 오면 하나가 실패할 수 있습니다. 한 사람이 `/목소리` 를 동시에 두 번 하는 일이라 받아들입니다.
- 허용 목록에 이미 있는지는 `existsById` 로 보고 넣습니다. 그 사이 다른 요청이 같은 디스코드 서버를 넣으면 기본 키 때문에 커밋에서 실패합니다.
- 같은 키를 넣다 실패하면 예외가 `DuplicateKeyException` 대신 그 부모인 `DataIntegrityViolationException` 입니다.
- 매퍼 클래스는 웹 어댑터(ADR 0030)와 저장소 어댑터 두 자리에 둡니다. 애그리거트 → 유스케이스 결과 Dto 는 지금처럼 Dto 의 `from` 이 합니다(ADR 0010).

## 고르지 않은 것

- `JdbcClient` 그대로: 이 제품에는 충분했지만, 처음 보는 사람이 기대하는 모양이 아닙니다.
- 도메인 클래스에 `@Entity` 를 바로 붙이기: 파일은 줄지만 도메인이 JPA 를 알게 되고, JPA 가 요구하는 기본 생성자 때문에 필드를 `final` 로 둘 수 없습니다. 도메인 규칙(ADR 0035)도 깨집니다.
- Spring Data JDBC: 애그리거트 단위로 저장해서 이 구조와 잘 맞고 가볍지만, 흔히 쓰는 쪽은 JPA 입니다.
- 매퍼를 스프링 빈으로 두기: 상태도 바꿔 끼울 구현도 없어서 주입할 이유가 없고, 웹 매퍼와 모양이 달라집니다.
- 모든 엔티티를 `save`(있으면 고치기)로 두기: 코드는 짧지만 `saveAll` 이 이미 있는 줄을 조용히 덮어써서 포트의 약속과 달라집니다.
