-- 봇 주인이 /허용목록 으로 넣은 디스코드 서버. 허용 목록을 켜 두면 여기 없는 디스코드 서버에서는 봇이 나간다.
-- Flyway 전에 schema.sql 로 이 테이블까지 만든 H2 파일도 있어서, 이미 있으면 건너뛴다.
create table if not exists allowed_guild (
    guild_id   bigint      not null primary key,
    allowed_at datetime(6) not null
);
