package io.github.phs314.quicktts.common;

/**
 * 데스크톱 클라이언트와 봇 서버가 함께 쓰는 API 약속.
 */
public final class QuickChatApi {

    /** quick chat 문장을 보내는 엔드포인트 경로. 기기 토큰이 필요하다. */
    public static final String PATH = "/api/quick-chat";

    /** 지금 quick chat 을 보내면 어느 음성 채널에서 읽힐지 알려 주는 경로. 기기 토큰이 필요하고, 음성 채널에 없으면 204. */
    public static final String MY_VOICE_CHANNEL_PATH = "/api/me/voice-channel";

    /** 연결 코드로 PC 를 등록하고 기기 토큰을 받는 엔드포인트 경로. */
    public static final String DEVICES_PATH = "/api/devices";

    /** 기기 토큰을 {@code Authorization} 헤더에 실을 때 붙이는 접두사. */
    public static final String BEARER_PREFIX = "Bearer ";

    /** 한 번에 읽어 줄 수 있는 최대 글자 수. */
    public static final int MAX_TEXT_LENGTH = 200;

    private QuickChatApi() {
    }
}
