package io.github.phs314.quicktts.bot.guild.application.service;

import io.github.phs314.quicktts.bot.guild.application.port.in.command.LeaveIfNotAllowedCommand;
import io.github.phs314.quicktts.bot.guild.application.port.in.usecase.EnforceGuildAllowlistUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.GuildAllowlistSettingPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.JoinedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.guild.domain.vo.GuildAllowlist;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnforceGuildAllowlistService implements EnforceGuildAllowlistUseCase {

    private final AllowedGuildPort allowedGuildPort;
    private final JoinedGuildPort joinedGuildPort;
    private final GuildAllowlistSettingPort guildAllowlistSettingPort;
    private final Clock clock;

    @Override
    public void leaveIfNotAllowed(LeaveIfNotAllowedCommand command) {
        if (!currentAllowlist().permits(command.guildId())) {
            joinedGuildPort.leaveWithNotice(command.guildId());
        }
    }

    /**
     * 처음 채울 때는 봇이 들어가 있는 디스코드 서버를 한꺼번에 넣는다 (ADR 0028). 일부만 남으면 다음에 켤 때
     * 목록이 비어 있지 않아서 나머지 디스코드 서버에서 나가게 되므로 하나씩 넣지 않는다.
     */
    @Override
    public void leaveAllNotAllowed() {
        GuildAllowlist allowlist = currentAllowlist();
        List<GuildId> joined = joinedGuildPort.joinedGuilds();
        if (allowlist.needsFirstFill()) {
            Instant now = clock.instant();
            allowedGuildPort.saveAll(joined.stream().map(guildId -> AllowedGuild.allow(guildId, now)).toList());
            return;
        }
        allowlist.notPermitted(joined).forEach(joinedGuildPort::leaveWithNotice);
    }

    private GuildAllowlist currentAllowlist() {
        return new GuildAllowlist(guildAllowlistSettingPort.isEnabled(),
                allowedGuildPort.findAll().stream().map(AllowedGuild::guildId).collect(Collectors.toSet()));
    }
}
