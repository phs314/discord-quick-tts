# 도메인 모델을 작게 바로잡는다: 채널 ID, 화면용 채널 정보, 되살리기, 음성 배열, 허용 목록 확인

- 상태: 채택
- 날짜: 2026-11-01

컨텍스트를 나누고 나서 도메인 모델을 다시 훑어보니 추상화를 더할 곳이 아니라 바로잡을 곳이 다섯 있었습니다.

- 음성 채널(`VoiceChannel`)의 디스코드 서버 ID 와 사용자 ID 는 값 객체로 감쌌는데, 채널 ID 만 날 `long` 이었습니다.
- 화면에 보여 줄 서버 이름, 아이콘 주소, 채널 이름(`VoiceChannelDetails`)이 speech 도메인의 값 객체였습니다. 규칙에 쓰이지 않는 표시용 값이 도메인에 있으면 도메인이 화면을 따라 바뀝니다.
- `Device` 와 `AllowedGuild` 는 저장소에서 되살릴 때 쓰는 생성자가 공개라, 새로 만들 때의 규칙(`register`, `allow`)을 건너뛰고 만들 수 있었습니다. `Pairing` 은 생성자를 감추고 `restore` 를 써서 셋이 서로 달랐습니다.
- 음성(`Audio`)은 `byte[]` 를 그대로 든 record 라서 받은 쪽이 배열을 고치면 캐시에 있는 음성까지 바뀝니다. 지금은 "읽기만 한다" 는 주석으로만 막고 있었습니다.
- 봇이 새 디스코드 서버에 초대될 때마다 허용 목록 전체를 읽어 그 서버 하나가 들어 있는지 봤습니다. 허용한 서버가 많아지면 초대 한 번에 그만큼 읽습니다.

## 결정

1. **채널 ID 를 `ChannelId` 값 객체로 감쌉니다** (speech `domain/vo`). 이름은 디스코드 API 의 `channel_id` 를 따릅니다(ADR 0044). 디스코드 서버 ID, 사용자 ID 와 같은 모양이라 바꿔 넣지 않게 하려는 것이고, 규칙은 `GuildId` 와 같습니다(양수).
2. **`VoiceChannelDetails` 를 `application/port/in/dto/VoiceChannelDto` 로 옮깁니다.** 유스케이스 `FindCurrentUserVoiceChannel` 이 돌려주는 조회 결과라서 ADR 0010 대로 `...Dto` 입니다. 디스코드에서 읽어 오는 출력 포트(`VoiceStatePort`)도 같은 Dto 를 돌려줍니다. 같은 값을 두 번 옮겨 담는 타입을 하나 더 두지 않습니다.
3. **저장소에서 되살릴 때는 `restore(...)` 를 씁니다.** `Device.restore`, `AllowedGuild.restore`, `Pairing.restore` 로 맞추고 생성자는 감춥니다. 새로 만들 때는 지금처럼 `register`, `allow`, `issue` 입니다.
4. **`Audio` 는 배열을 받을 때와 내줄 때 복사합니다** (speech, synthesis 둘 다). 형식은 mp3 하나뿐이라 enum 으로 바꾸지 않고 문자열 그대로 둡니다.
5. **디스코드 서버 하나를 확인할 때는 그 서버가 허용 목록에 있는지만 읽습니다** (`AllowedGuildPort.exists`). 머물러도 되는지는 지금처럼 `GuildAllowlist.allows` 가 정합니다. 목록 전체는 켜질 때 들어가 있는 서버를 모두 훑을 때만 읽습니다.

## 결과

- 동작, 웹 API, DB 는 바뀌지 않습니다.
- 짧은 음성(수십 KB)을 재생할 때 한 번씩 더 복사합니다.
- `AllowedGuildPort` 에 메서드가 하나 늘었습니다.

## 고르지 않은 것

- `VoiceChannelId`: 디스코드 API 는 음성, 스테이지, 글 채널 모두 `channel_id` 라고 부릅니다. 채널 종류는 `VoiceChannel` 이 이미 말해 줍니다.
- 출력 포트용 읽기 모델을 따로 두고 서비스에서 Dto 로 옮기기: 필드가 똑같은 타입이 하나 더 생기고 옮기는 코드만 늘어납니다.
- 허용 목록 확인을 `AllowlistPolicy` 같은 새 도메인 객체로 나누기: 규칙은 `GuildAllowlist` 하나에 이미 있고, 바뀐 것은 읽는 양뿐입니다.
