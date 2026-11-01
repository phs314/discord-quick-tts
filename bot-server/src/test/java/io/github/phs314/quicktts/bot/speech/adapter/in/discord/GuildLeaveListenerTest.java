package io.github.phs314.quicktts.bot.speech.adapter.in.discord;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.in.VacateBotSeatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.VacateBotSeatCommand;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import org.junit.jupiter.api.Test;

class GuildLeaveListenerTest {

    private final VacateBotSeatUseCase vacateBotSeatUseCase = mock(VacateBotSeatUseCase.class);
    private final GuildLeaveListener listener = new GuildLeaveListener(vacateBotSeatUseCase);

    @Test
    void 봇이_디스코드_서버에서_나가면_그_서버의_자리를_비운다() {
        GuildLeaveEvent event = mock(GuildLeaveEvent.class, RETURNS_DEEP_STUBS);
        when(event.getGuild().getIdLong()).thenReturn(100L);

        listener.onGuildLeave(event);

        verify(vacateBotSeatUseCase).vacate(new VacateBotSeatCommand(new GuildId(100L)));
    }
}
