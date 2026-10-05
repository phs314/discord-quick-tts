package io.github.phs314.quicktts.common;

/**
 * 데스크톱 클라이언트와 봇 서버가 함께 쓰는 quick chat API 약속.
 */
public final class QuickChatApi {

    /** quick chat 문장을 보내는 엔드포인트 경로. */
    public static final String PATH = "/api/quick-chat";

    /** 클라이언트 인증용 API 키를 담는 헤더 이름. */
    public static final String API_KEY_HEADER = "X-Api-Key";

    /** 한 번에 읽어 줄 수 있는 최대 글자 수. */
    public static final int MAX_TEXT_LENGTH = 200;

    private QuickChatApi() {
    }
}
