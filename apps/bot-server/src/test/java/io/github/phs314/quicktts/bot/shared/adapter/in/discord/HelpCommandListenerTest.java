package io.github.phs314.quicktts.bot.shared.adapter.in.discord;

import static org.assertj.core.api.Assertions.assertThat;

import net.dv8tion.jda.api.entities.Message;
import org.junit.jupiter.api.Test;

class HelpCommandListenerTest {

    @Test
    void 안내는_디스코드_메시지_한_개에_들어간다() {
        assertThat(HelpCommandListener.HELP_MESSAGE).hasSizeLessThanOrEqualTo(Message.MAX_CONTENT_LENGTH);
    }
}
