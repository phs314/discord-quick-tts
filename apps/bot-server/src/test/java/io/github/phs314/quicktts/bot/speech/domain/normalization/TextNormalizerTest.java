package io.github.phs314.quicktts.bot.speech.domain.normalization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;
import io.github.phs314.quicktts.bot.speech.domain.vo.QuickChatMessage;
import org.junit.jupiter.api.Test;

class TextNormalizerTest {

    @Test
    void 자음은_정한_소리로_반복된_만큼_읽는다() {
        assertThat(spoken("ㅋㅋㅋ 웃기다 ㅎㅎ")).isEqualTo("키키키 웃기다 히히");
        assertThat(spoken("ㅇㅇ ㄴㄴ ㄱㄱ")).isEqualTo("이응이응 니은니은 고고");
    }

    @Test
    void 정하지_않은_자음과_모음은_그대로_둔다() {
        assertThat(spoken("ㅠㅠ ㄷㄷ")).isEqualTo("ㅠㅠ ㄷㄷ");
    }

    @Test
    void 완성된_글자는_건드리지_않는다() {
        assertThat(spoken("강남 가자")).isEqualTo("강남 가자");
    }

    @Test
    void 이모지는_지운다() {
        assertThat(spoken("좋아 👍 진짜😂")).isEqualTo("좋아 진짜");
        assertThat(spoken("가족 👨‍👩‍👧 손 👍🏽 국기 🇰🇷 하트 ❤️")).isEqualTo("가족 손 국기 하트");
    }

    @Test
    void 숫자와_문장부호는_이모지로_보지_않는다() {
        assertThat(spoken("3시 #1 * 됐다!!!")).isEqualTo("3시 #1 * 됐다!!!");
    }

    @Test
    void 링크는_링크를_보냈어요로_읽는다() {
        assertThat(spoken("이거 봐 https://youtu.be/abc?t=1 ㅋㅋ")).isEqualTo("이거 봐 링크를 보냈어요 키키");
        assertThat(spoken("www.naver.com")).isEqualTo("링크를 보냈어요");
        assertThat(spoken("여기HTTP://a.b/c")).isEqualTo("여기링크를 보냈어요");
    }

    @Test
    void 반복된_글자는_줄이지_않는다() {
        assertThat(spoken("와아아아아 !!!!!")).isEqualTo("와아아아아 !!!!!");
    }

    @Test
    void 다듬고_나서_비면_읽을_수_없다() {
        assertThatThrownBy(() -> TextNormalizer.normalize(new QuickChatMessage("😂 👍")))
                .isInstanceOf(InvalidDomainValueException.class)
                .hasMessage("읽을 내용이 없습니다.");
    }

    @Test
    void 글자_수는_보낸_문장_그대로_센다() {
        String link = "https://example.com/" + "a".repeat(QuickChatMessage.MAX_LENGTH - 20);

        assertThat(new QuickChatMessage(link).text()).hasSize(QuickChatMessage.MAX_LENGTH);
        assertThat(spoken(link)).isEqualTo("링크를 보냈어요");
    }

    private static String spoken(String text) {
        return TextNormalizer.normalize(new QuickChatMessage(text)).text();
    }
}
