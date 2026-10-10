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
    public static final String DEVICE_UNAUTHORIZED = "device-unauthorized";

    /** 보낸 사람이 봇이 있는 디스코드 서버의 음성 채널에 없음. 409. */
    public static final String SPEAKER_NOT_IN_VOICE_CHANNEL = "speaker-not-in-voice-channel";

    /** 봇이 같은 디스코드 서버의 다른 음성 채널에서 쓰이고 있음. 423. */
    public static final String VOICE_CHANNEL_IN_USE = "voice-channel-in-use";

    /** TTS 엔진이 음성을 만들지 못함. 502. */
    public static final String SPEECH_SYNTHESIS_FAILED = "speech-synthesis-failed";

    private ApiErrorCode() {
    }
}
