-- /연결 로 발급한 연결 코드. 5분 동안만 쓸 수 있고 한 번 쓰면 지운다.
-- 메모리에 두면 봇 서버를 다시 켜거나 봇과 API 를 다른 프로세스로 나눌 때 코드가 사라지므로 DB 에 둔다.
create table pairing (
    code            varchar(8)  not null primary key,
    discord_user_id bigint      not null,
    expires_at      datetime(6) not null
);

-- 새 코드를 저장할 때 만료된 줄을 지운다.
create index idx_pairing_expires_at on pairing (expires_at);
