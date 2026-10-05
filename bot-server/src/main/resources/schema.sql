create table if not exists device (
    token_hash      varchar(64) primary key,
    discord_user_id bigint      not null,
    registered_at   timestamp   not null
);

-- 기기 해제 기능에서 추가한 열. 이전에 등록된 기기는 식별자를 새로 채우고, 이름은 비워 두면 기본 이름으로 보인다.
alter table device add column if not exists id varchar(36);
alter table device add column if not exists name varchar(50);
update device set id = cast(random_uuid() as varchar) where id is null;

create unique index if not exists ux_device_id on device (id);
create index if not exists idx_device_discord_user_id on device (discord_user_id);
