package io.github.phs314.quicktts.bot.speech.adapter.in.discord;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.in.VacateBotSeatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.VacateBotSeatCommand;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

/**
 * 봇이 디스코드 서버에서 나갔다는 소식을 봇 자리에 알린다. 허용 목록에서 빠졌거나 관리자가 봇을 내보낸 경우다.
 * 이때 디스코드는 봇의 음성 채널 소식을 따로 보내지 않아서 {@link VoiceStateListener} 로는 알 수 없다.
 */
@Component
@RequiredArgsConstructor
public class GuildLeaveListener extends ListenerAdapter {

    private final VacateBotSeatUseCase vacateBotSeatUseCase;

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        vacateBotSeatUseCase.vacate(new VacateBotSeatCommand(new GuildId(event.getGuild().getIdLong())));
    }
}
