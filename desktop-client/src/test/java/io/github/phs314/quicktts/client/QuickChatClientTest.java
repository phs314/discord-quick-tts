package io.github.phs314.quicktts.client;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.common.ApiErrorCode;
import org.junit.jupiter.api.Test;

/**
 * 서버 오류 응답의 오류 코드로 안내 문구를 고르는지 확인한다. 서버에 요청은 보내지 않는다.
 */
class QuickChatClientTest {

    private final QuickChatClient client = new QuickChatClient("http://localhost:0");

    @Test
    void 해제된_기기면_다시_연결하라는_예외() {
        assertThat(client.sendFailure(401, problem(ApiErrorCode.DEVICE_UNAUTHORIZED, "기기 토큰이 없습니다.")))
                .isInstanceOf(QuickChatClient.DeviceUnauthorizedException.class);
    }

    @Test
    void 문장이_규칙에_안_맞으면_서버가_준_이유를_보여_준다() {
        assertThat(client.sendFailure(400, problem(ApiErrorCode.INVALID_VALUE, "문장이 비어 있습니다.")))
                .hasMessage("문장이 비어 있습니다.");
    }

    @Test
    void 음성_채널에_없으면_먼저_들어가라고_안내한다() {
        assertThat(client.sendFailure(409, problem(ApiErrorCode.SPEAKER_NOT_IN_VOICE_CHANNEL, "x")))
                .hasMessage("봇이 있는 서버의 음성 채널에 먼저 들어가 주세요.");
    }

    @Test
    void 봇이_다른_채널에_있으면_그_채널이_비면_쓸_수_있다고_안내한다() {
        assertThat(client.sendFailure(423, problem(ApiErrorCode.VOICE_CHANNEL_IN_USE, "x")).getMessage())
                .contains("다른 음성 채널");
    }

    @Test
    void 음성_생성에_실패하면_다시_보내라고_안내한다() {
        assertThat(client.sendFailure(502, problem(ApiErrorCode.SPEECH_SYNTHESIS_FAILED, "x")).getMessage())
                .contains("음성을 만들지 못했습니다");
    }

    @Test
    void 오류_코드가_없으면_상태_코드를_보여_준다() {
        assertThat(client.sendFailure(500, "<html>오류</html>")).hasMessageContaining("HTTP 500");
        assertThat(client.sendFailure(404, "")).hasMessageContaining("HTTP 404");
    }

    @Test
    void 연결_코드가_틀리면_다시_입력하라고_안내한다() {
        assertThat(client.registerFailure(400, problem(ApiErrorCode.INVALID_PAIRING_CODE, "x")).getMessage())
                .contains("/연결 을 다시 입력");
        assertThat(client.registerFailure(400, problem(ApiErrorCode.INVALID_VALUE, "연결 코드 형식이 아닙니다.")).getMessage())
                .contains("/연결 을 다시 입력");
    }

    private static String problem(String code, String detail) {
        return "{\"type\":\"about:blank\",\"title\":\"t\",\"status\":400,\"detail\":\"" + detail
                + "\",\"instance\":\"/api/v1/quick-chat\",\"" + ApiErrorCode.PROPERTY + "\":\"" + code + "\"}";
    }
}
