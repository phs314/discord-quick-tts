-- 첫 스키마. H2(MySQL 호환 모드)와 MySQL 양쪽에서 돌도록 두 DB 가 함께 아는 문법만 쓴다.
-- 바꿀 때는 이 파일을 고치지 말고 다음 번호(V2__...)로 새 파일을 더한다.

-- 등록된 기기. 토큰은 SHA-256 해시로만 저장한다.
create table device (
    token_hash      varchar(64) not null primary key,
    id              varchar(36) not null,
    discord_user_id bigint      not null,
    name            varchar(50),
    registered_at   datetime(6) not null
);

create unique index ux_device_id on device (id);
create index idx_device_discord_user_id on device (discord_user_id);

-- 사용자가 /목소리 로 고른 목소리. 없으면 기본 목소리를 쓴다.
create table voice_preference (
    discord_user_id bigint       not null primary key,
    voice_id        varchar(100) not null
);
