package io.github.phs314.quicktts.bot.guild.adapter.in.discord;

import io.github.phs314.quicktts.bot.guild.application.port.in.usecase.EnforceGuildAllowlistUseCase;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

/**
 * 봇이 디스코드 서버에 초대될 때와 봇 서버가 켜질 때 허용 목록을 확인하게 한다.
 */
@Component
@RequiredArgsConstructor
public class GuildJoinListener extends ListenerAdapter {

    private final JDA jda;
    private final EnforceGuildAllowlistUseCase enforceGuildAllowlistUseCase;

    @PostConstruct
    void register() {
        jda.addEventListener(this);
        // JDA 는 이미 준비된 상태로 만들어지므로, 꺼져 있는 동안 초대된 디스코드 서버는 여기서 확인한다.
        enforceGuildAllowlistUseCase.checkAllJoinedGuilds();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        enforceGuildAllowlistUseCase.checkJoinedGuild(new GuildId(event.getGuild().getIdLong()));
    }
}
