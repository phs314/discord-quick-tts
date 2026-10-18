package io.github.phs314.quicktts.bot.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.phs314.quicktts.bot.shared.adapter.in.discord.DiscordSlashCommand;
import io.github.phs314.quicktts.bot.shared.adapter.in.discord.FailureReplyingListener;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DiscordListenerRegistrarTest {

    private final JDA jda = mock(JDA.class, RETURNS_DEEP_STUBS);

    @Test
    void 리스너마다_한_번씩_등록하고_슬래시_명령만_실패_답장으로_감싼다() {
        EventListener listener = new ListenerAdapter() {
        };
        DiscordSlashCommand command = new DiscordSlashCommand() {
            @Override
            public SlashCommandData definition() {
                return Commands.slash("시험", "시험 명령");
            }

            @Override
            public void onEvent(GenericEvent event) {
            }
        };

        new DiscordListenerRegistrar(jda, List.of(listener, command), List.of(command)).register();

        ArgumentCaptor<Object> registered = ArgumentCaptor.forClass(Object.class);
        verify(jda, times(2)).addEventListener(registered.capture());
        assertThat(registered.getAllValues().getFirst()).isSameAs(listener);
        assertThat(registered.getAllValues().get(1)).isInstanceOf(FailureReplyingListener.class);
    }
}
