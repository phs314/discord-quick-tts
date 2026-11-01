package io.github.phs314.quicktts.api;

/**
 * 데스크톱 클라이언트와 봇 서버가 함께 쓰는 API 약속.
 */
public final class QuickTtsApi {

    /**
     * 모든 경로 앞에 붙는 버전. 배포된 클라이언트가 깨지는 방식으로 요청·응답 모양을 바꿀 때만 올리고,
     * 그동안 옛 버전 경로도 함께 살려 둔다.
     */
    public static final String VERSION_PREFIX = "/api/v1";

    /** quick chat 문장을 보내는 엔드포인트 경로. 기기 토큰이 필요하다. */
    public static final String QUICK_CHAT_PATH = VERSION_PREFIX + "/quick-chat";

    /** 지금 quick chat 을 보내면 어느 음성 채널에서 읽힐지 알려 주는 경로. 기기 토큰이 필요하고, 음성 채널에 없으면 204. */
    public static final String MY_VOICE_CHANNEL_PATH = VERSION_PREFIX + "/me/voice-channel";

    /** 연결 코드로 PC 를 등록하고 기기 토큰을 받는 엔드포인트 경로. */
    public static final String DEVICES_PATH = VERSION_PREFIX + "/devices";

    /** 서버가 떠 있는지 보는 상태 확인 경로. 토큰 없이 열려 있고, 디스코드에 붙어 있지 않으면 503 이다. */
    public static final String HEALTH_PATH = "/actuator/health";

    /** 기기 토큰을 {@code Authorization} 헤더에 실을 때 붙이는 접두사. */
    public static final String BEARER_PREFIX = "Bearer ";

    /** 한 번에 읽어 줄 수 있는 최대 글자 수. */
    public static final int MAX_CONTENT_LENGTH = 200;

    private QuickTtsApi() {
    }
}
