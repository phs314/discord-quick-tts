package io.github.phs314.quicktts.bot.guild.adapter.in.discord;

import io.github.phs314.quicktts.bot.guild.application.port.in.SeedAllowlistUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.in.LeaveAllNotAllowedGuildsUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.in.LeaveNotAllowedGuildUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.LeaveNotAllowedGuildCommand;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 봇이 디스코드 서버에 초대될 때와 봇 서버가 켜질 때 허용 목록을 확인하게 한다.
 */
@Component
@RequiredArgsConstructor
public class GuildJoinListener extends ListenerAdapter {

    private final SeedAllowlistUseCase seedAllowlistUseCase;
    private final LeaveAllNotAllowedGuildsUseCase leaveAllNotAllowedGuildsUseCase;
    private final LeaveNotAllowedGuildUseCase leaveNotAllowedGuildUseCase;

    /**
     * ShardManager 는 모든 샤드가 준비된 뒤에 만들어지므로, 꺼져 있는 동안 초대된 디스코드 서버는 봇 서버가 켜졌을 때 여기서 확인한다.
     * 허용 목록 채우기(DB 트랜잭션)가 끝난 뒤에 나가기(되돌릴 수 없는 호출)를 따로 부른다 (ADR 0033).
     */
    @EventListener(ApplicationReadyEvent.class)
    void leaveNotAllowedGuildsOnStartup() {
        seedAllowlistUseCase.seedIfEmpty();
        leaveAllNotAllowedGuildsUseCase.leaveAllNotAllowed();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        GuildId guildId = new GuildId(event.getGuild().getIdLong());
        leaveNotAllowedGuildUseCase.leaveIfNotAllowed(new LeaveNotAllowedGuildCommand(guildId));
    }
}
