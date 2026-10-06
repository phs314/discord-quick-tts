package io.github.phs314.quicktts.bot.speech.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;
import io.github.phs314.quicktts.common.QuickChatApi;
import org.junit.jupiter.api.Test;

class QuickChatMessageTest {

    @Test
    void 앞뒤_공백을_지운다() {
        assertThat(new QuickChatMessage("  안녕하세요 ").text()).isEqualTo("안녕하세요");
    }

    @Test
    void 빈_문장은_만들_수_없다() {
        assertThatThrownBy(() -> new QuickChatMessage("   "))
                .isInstanceOf(InvalidDomainValueException.class);
    }

    @Test
    void 최대_길이를_넘는_문장은_만들_수_없다() {
        assertThat(new QuickChatMessage("가".repeat(QuickChatMessage.MAX_LENGTH)).text())
                .hasSize(QuickChatMessage.MAX_LENGTH);
        assertThatThrownBy(() -> new QuickChatMessage("가".repeat(QuickChatMessage.MAX_LENGTH + 1)))
                .isInstanceOf(InvalidDomainValueException.class);
    }

    @Test
    void 클라이언트와_같은_최대_길이를_쓴다() {
        assertThat(QuickChatMessage.MAX_LENGTH).isEqualTo(QuickChatApi.MAX_TEXT_LENGTH);
    }
}
