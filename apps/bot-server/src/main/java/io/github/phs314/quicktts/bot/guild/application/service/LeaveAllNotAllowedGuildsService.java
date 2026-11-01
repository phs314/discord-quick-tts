package io.github.phs314.quicktts.bot.guild.application.service;

import io.github.phs314.quicktts.bot.guild.application.port.in.LeaveAllNotAllowedGuildsUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.GuildAllowlistConfigPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.JoinedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.guild.domain.vo.GuildAllowlist;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveAllNotAllowedGuildsService implements LeaveAllNotAllowedGuildsUseCase {

    private final AllowedGuildPort allowedGuildPort;
    private final JoinedGuildPort joinedGuildPort;
    private final GuildAllowlistConfigPort guildAllowlistConfigPort;

    @Override
    public void leaveAllNotAllowed() {
        GuildAllowlist allowlist = currentAllowlist();
        if (allowlist.needsSeeding()) {
            return;
        }
        allowlist.notAllowed(joinedGuildPort.joinedGuilds()).forEach(joinedGuildPort::leaveWithNotice);
    }

    private GuildAllowlist currentAllowlist() {
        return new GuildAllowlist(guildAllowlistConfigPort.isEnabled(),
                allowedGuildPort.findAll().stream().map(AllowedGuild::guildId).collect(Collectors.toSet()));
    }
}
