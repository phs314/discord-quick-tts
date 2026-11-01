package io.github.phs314.quicktts.bot.guild.application.service;

import io.github.phs314.quicktts.bot.guild.application.port.in.LeaveNotAllowedGuildUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.LeaveNotAllowedGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.GuildAllowlistConfigPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.JoinedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.vo.GuildAllowlist;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveNotAllowedGuildService implements LeaveNotAllowedGuildUseCase {

    private final AllowedGuildPort allowedGuildPort;
    private final JoinedGuildPort joinedGuildPort;
    private final GuildAllowlistConfigPort guildAllowlistConfigPort;

    @Override
    public void leaveIfNotAllowed(LeaveNotAllowedGuildCommand command) {
        if (!allowlistFor(command.guildId()).allows(command.guildId())) {
            joinedGuildPort.leaveWithNotice(command.guildId());
        }
    }

    /** 허용 목록 전체 대신 이 디스코드 서버가 들어 있는지만 읽는다. 머물러도 되는지는 {@link GuildAllowlist} 가 정한다. */
    private GuildAllowlist allowlistFor(GuildId guildId) {
        return new GuildAllowlist(guildAllowlistConfigPort.isEnabled(),
                allowedGuildPort.exists(guildId) ? Set.of(guildId) : Set.of());
    }
}
