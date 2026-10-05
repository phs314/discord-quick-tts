create table if not exists device (
    token_hash      varchar(64) primary key,
    discord_user_id bigint      not null,
    registered_at   timestamp   not null
);

create index if not exists idx_device_discord_user_id on device (discord_user_id);
