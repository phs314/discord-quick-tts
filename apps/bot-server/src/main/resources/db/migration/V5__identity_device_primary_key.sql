-- 기기의 기본 키를 토큰 해시에서 기기 ID 로 바꾼다. 토큰을 새로 발급할 때 줄을 지우고 다시 만들지 않아도 되게 하려는 것이다.
-- 토큰 해시는 요청마다 기기를 찾는 데 쓰므로 유일 인덱스로 남긴다. 기기 ID 의 유일 인덱스는 기본 키가 대신한다.
-- Flyway 전 schema.sql 로 만든 옛 H2 파일은 id 열이 나중에 더해져 null 을 허용하므로 먼저 막는다.
alter table device modify id varchar(36) not null;
alter table device drop primary key;
alter table device add primary key (id);
create unique index ux_device_token_hash on device (token_hash);
drop index ux_device_id on device;
