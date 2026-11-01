package io.github.phs314.quicktts.common;

/**
 * 봇 서버가 오류 응답(ProblemDetail)의 {@link #PROPERTY} 속성에 싣는 오류 코드.
 * 클라이언트는 HTTP 상태 코드 대신 이 값으로 안내 문구를 고른다. 같은 상태 코드도 뜻이 여럿일 수 있기 때문이다.
 * 이미 배포된 클라이언트가 알고 있으므로 값을 바꾸거나 지우지 않고, 필요하면 새 코드를 더한다.
 */
public final class ApiErrorCode {

    /** 오류 코드가 들어 있는 ProblemDetail 속성 이름. */
    public static final String PROPERTY = "code";

    /** 요청 값이 규칙에 맞지 않음 (빈 문장, 너무 긴 문장, 연결 코드 형식 등). 400. {@code detail} 에 이유가 있다. */
    public static final String INVALID_VALUE = "invalid-value";

    /** 없거나 만료된 연결 코드. 400. */
    public static final String INVALID_PAIRING_CODE = "invalid-pairing-code";

    /** 기기 토큰이 없거나 해제된 기기. 401. 클라이언트는 연결 코드를 다시 받아야 한다. */
    public static final String INVALID_DEVICE_TOKEN = "invalid-device-token";

    /** 보낸 사람이 봇이 있는 디스코드 서버의 음성 채널에 없음. 409. */
    public static final String AUTHOR_NOT_IN_VOICE_CHANNEL = "author-not-in-voice-channel";

    /** 봇이 같은 디스코드 서버의 다른 음성 채널에서 쓰이고 있음. 409 (옛 봇 서버는 423). */
    public static final String VOICE_CHANNEL_IN_USE = "voice-channel-in-use";

    /** 보낸 사람의 음성 채널에 봇이 들어갈 수 없음 (채널 인원이 꽉 참, 봇에게 연결 권한 없음). 403. */
    public static final String BOT_CANNOT_JOIN_VOICE_CHANNEL = "bot-cannot-join-voice-channel";

    /** TTS 엔진이 음성을 만들지 못함. 502. */
    public static final String SPEECH_SYNTHESIS_FAILED = "speech-synthesis-failed";

    /** 고를 수 있는 목소리 목록에 없는 목소리. 400. 지금은 디스코드 {@code /목소리} 에서만 생긴다. */
    public static final String UNKNOWN_VOICE = "unknown-voice";

    /** 봇 주인만 할 수 있는 일. 403. 지금은 디스코드 {@code /허용목록} 에서만 생긴다. */
    public static final String NOT_BOT_OWNER = "not-bot-owner";

    /** 요청 모양이 틀림 (JSON 이 깨짐, 없는 경로, 맞지 않는 메서드 등). 4xx. 클라이언트 버그라서 따로 안내하지 않는다. */
    public static final String INVALID_REQUEST = "invalid-request";

    /** 봇 서버가 예상하지 못한 실패. 500. 봇 서버 로그에 원인이 남는다. */
    public static final String UNEXPECTED_FAILURE = "unexpected-failure";

    private ApiErrorCode() {
    }
}
