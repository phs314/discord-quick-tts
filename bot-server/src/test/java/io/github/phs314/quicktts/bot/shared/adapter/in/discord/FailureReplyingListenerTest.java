package io.github.phs314.quicktts.bot.shared.adapter.in.discord;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FailureReplyingListenerTest {

    private final SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);
    private final ReplyCallbackAction reply = mock(ReplyCallbackAction.class);

    @BeforeEach
    void setUp() {
        when(event.reply(anyString())).thenReturn(reply);
        when(reply.setEphemeral(anyBoolean())).thenReturn(reply);
    }

    @Test
    void 예상한_실패는_그_문구로_답한다() {
        new FailureReplyingListener(e -> {
            throw new InvalidDomainValueException("문장이 비어 있습니다.");
        }).onEvent(event);

        verify(event).reply("문장이 비어 있습니다.");
        verify(reply).setEphemeral(true);
        verify(reply).queue();
    }

    @Test
    void 예상하지_못한_실패는_다시_해_달라고_답하고_더_던지지_않는다() {
        FailureReplyingListener listener = new FailureReplyingListener(e -> {
            throw new IllegalStateException("버그");
        });

        assertThatCode(() -> listener.onEvent(event)).doesNotThrowAnyException();
        verify(event).reply(FailureReplyingListener.UNEXPECTED_FAILURE_REPLY);
    }

    @Test
    void 이미_답한_뒤의_실패에는_다시_답하지_않는다() {
        when(event.isAcknowledged()).thenReturn(true);

        new FailureReplyingListener(e -> {
            throw new IllegalStateException("답한 뒤의 버그");
        }).onEvent(event);

        verify(event, never()).reply(anyString());
    }
}
