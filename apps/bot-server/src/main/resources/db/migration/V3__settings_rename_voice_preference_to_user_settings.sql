-- 사용자가 고른 목소리 테이블을 사용자 설정(settings 컨텍스트)으로 이름만 바꾼다. 줄과 열은 그대로다.
-- 목소리 말고 다른 설정(읽는 속도 같은)이 생기면 이 테이블에 열을 더한다.
alter table voice_preference rename to user_settings;
