package io.github.phs314.quicktts.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.Test;

/**
 * GitHub 최신 Release 응답을 보고 새 버전인지 가리는지 확인한다. GitHub 에 요청은 보내지 않는다.
 */
class UpdateCheckerTest {

    private static final String PAGE = "https://github.com/phs314/discord-quick-tts/releases/tag/v0.2.0";

    @Test
    void 태그가_지금보다_높으면_새_버전과_Release_페이지를_돌려준다() {
        assertThat(UpdateChecker.newerRelease("0.1.0", release("v0.2.0", PAGE)))
                .contains(new UpdateChecker.Release("0.2.0", URI.create(PAGE)));
    }

    @Test
    void 같거나_낮은_버전이면_알리지_않는다() {
        assertThat(UpdateChecker.newerRelease("0.2.0", release("v0.2.0", PAGE))).isEmpty();
        assertThat(UpdateChecker.newerRelease("0.3.0", release("v0.2.0", PAGE))).isEmpty();
    }

    @Test
    void 버전은_글자가_아니라_숫자로_비교한다() {
        assertThat(UpdateChecker.isNewer("v0.10.0", "0.9.0")).isTrue();
        assertThat(UpdateChecker.isNewer("v1.0.0", "0.99.99")).isTrue();
        assertThat(UpdateChecker.isNewer("v0.1.2", "0.1.10")).isFalse();
    }

    @Test
    void 모양이_다른_태그나_응답은_새_버전으로_보지_않는다() {
        assertThat(UpdateChecker.isNewer("latest", "0.1.0")).isFalse();
        assertThat(UpdateChecker.isNewer("v1.0.0-beta", "0.1.0")).isFalse();
        assertThat(UpdateChecker.newerRelease("0.1.0", "{\"message\":\"Not Found\"}")).isEmpty();
        assertThat(UpdateChecker.newerRelease("0.1.0", "<html>")).isEmpty();
        assertThat(UpdateChecker.newerRelease("0.1.0", release("v0.2.0", ""))).isEmpty();
    }

    private static String release(String tag, String page) {
        return "{\"tag_name\":\"" + tag + "\",\"html_url\":\"" + page + "\",\"draft\":false,\"prerelease\":false}";
    }
}
