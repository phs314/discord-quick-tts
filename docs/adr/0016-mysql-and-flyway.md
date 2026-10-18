# 배포 서버는 MySQL, 스키마는 Flyway 로 관리한다

- 상태: 채택
- 날짜: 2026-10-11

지금까지 봇 서버는 H2 파일 DB 하나만 썼고, 켜질 때마다 `schema.sql` 을 처음부터 다시 돌렸습니다. 그래서 테이블을 바꿀 때마다 `alter table ... if not exists` 와 기존 행을 채우는 `update` 를 파일 뒤에 덧붙여야 했고, 어떤 DB 가 어디까지 바뀌었는지는 아무도 기록하지 않았습니다. 친구들이 쓰는 서버를 따로 띄우게 되면(배포), 지울 수 없는 데이터가 있는 DB 를 여러 번 고쳐 가야 합니다. 또 배포 서버의 DB 는 현수가 익숙한 MySQL 로 쓰기로 했습니다.

## 결정

- 배포 서버는 MySQL(8.4) 을 씁니다. `QUICKTTS_DB_URL=jdbc:mysql://.../quicktts?connectionTimeZone=UTC` 처럼 주소만 바꾸면 되고, 드라이버(`mysql-connector-j`)는 늘 들어 있습니다.
- 이 PC 에서 띄우는 서버(개발용 `bootRun`, exe 에 묶인 서버)는 지금처럼 설치가 필요 없는 H2 파일 DB 를 씁니다. 대신 MySQL 호환 모드(`;MODE=MySQL`)로 열어 같은 SQL 이 돌게 합니다.
- 스키마는 Flyway 마이그레이션 `bot-server/src/main/resources/db/migration/V<번호>__<설명>.sql` 로 관리합니다. 한 번 머지된 파일은 고치지 않고, 바꿀 것이 생기면 다음 번호 파일을 더합니다. `schema.sql` 과 `spring.sql.init` 은 없앴습니다.
- 마이그레이션과 저장소 SQL 은 H2(MySQL 모드)와 MySQL 이 함께 아는 문법만 씁니다. 예를 들어 "있으면 고치고 없으면 넣기" 는 H2 전용 `merge into ... key` 대신 `insert ... on duplicate key update` 를 씁니다.
- 저장소 테스트(`Jdbc...RepositoryTest`)는 `TestDatabase` 의 H2 와 MySQL 양쪽에서 같은 테스트를 돌립니다. MySQL 은 Testcontainers 로 띄우므로 Docker 가 필요합니다. CI 에서는 반드시 돌고, Docker 가 없는 개발 PC 에서는 MySQL 쪽만 건너뜁니다.

## 고르지 않은 것

- 이 PC 의 서버도 MySQL 로 바꾸기: 운영과 똑같아지지만, exe 를 켤 때마다 MySQL(또는 Docker Desktop)이 늘 켜져 있어야 합니다. 친구들에게 배포한 뒤에는 exe 에 서버를 묶을 필요가 없어서, 이 PC 의 DB 는 개발용으로만 남습니다. 두 DB 의 차이는 CI 의 MySQL 테스트가 잡습니다.
- JPA 로 바꾸기: SQL 이 몇 줄뿐이고 JdbcClient 로 충분합니다. DB 를 바꾸는 일과 묶을 이유가 없습니다.
- Liquibase: 같은 일을 하지만 SQL 파일을 그대로 쓰는 Flyway 가 더 단순합니다.

## 결과

- Flyway 전에 `schema.sql` 로 만든 H2 파일(저장소의 `data/`, `~/.discord-quick-tts/data/`)은 그대로 이어 씁니다. 테이블은 있는데 Flyway 기록이 없으면 처음 한 번 V1 을 건너뛴 것으로 표시하고(`spring.flyway.baseline-on-migrate`) V2 부터 돌립니다. 이 DB 의 `device.id` 는 예전처럼 null 을 허용한 채로 남지만, 예전 `schema.sql` 이 이미 모두 채워 두었습니다.
- 지금 H2 에 있는 기기와 목소리 설정을 MySQL 로 옮기는 도구는 만들지 않았습니다. 배포 서버로 옮길 때는 디스코드에서 `/연결` 과 `/목소리` 를 다시 하면 됩니다.
- 디스코드 서버 허용 목록(ADR 0015)의 `allowed_guild` 테이블은 `V2__create_allowed_guild.sql` 입니다. 그 PR 이 먼저 머지되어 `schema.sql` 로 이 테이블까지 만든 H2 파일도 있을 수 있어서, V2 만 `create table if not exists` 로 씁니다.
- 새 테이블을 더하는 작업은 다음 번호 파일을 더합니다. 두 작업이 같은 번호를 고르면 나중에 머지되는 쪽이 번호를 올립니다.
- exe 압축 파일이 MySQL 드라이버와 Flyway 만큼(약 3.5MB) 커집니다.
