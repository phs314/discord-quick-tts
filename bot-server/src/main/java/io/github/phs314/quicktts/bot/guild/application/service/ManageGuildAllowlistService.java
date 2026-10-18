package io.github.phs314.quicktts.bot.guild.application.service;

import io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.AllowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.DisallowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.in.dto.AllowedGuildDto;
import io.github.phs314.quicktts.bot.guild.application.port.in.dto.GuildAllowlistDto;
import io.github.phs314.quicktts.bot.guild.application.port.in.query.GetAllowlistQuery;
import io.github.phs314.quicktts.bot.guild.application.port.in.usecase.ManageGuildAllowlistUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.BotOwnerPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.GuildAllowlistSettingPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.JoinedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.guild.domain.vo.GuildAllowlist;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Clock;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManageGuildAllowlistService implements ManageGuildAllowlistUseCase {

    private final BotOwnerPort botOwnerPort;
    private final AllowedGuildPort allowedGuildPort;
    private final JoinedGuildPort joinedGuildPort;
    private final GuildAllowlistSettingPort guildAllowlistSettingPort;
    private final Clock clock;

    @Override
    public GuildAllowlistDto getAllowlist(GetAllowlistQuery query) {
        requireOwner(query.requester());
        return new GuildAllowlistDto(guildAllowlistSettingPort.isEnabled(),
                allowedGuildPort.findAll().stream().map(AllowedGuildDto::from).toList());
    }

    @Override
    public boolean allow(AllowGuildCommand command) {
        requireOwner(command.requester());
        return allowedGuildPort.saveIfAbsent(AllowedGuild.allow(command.guildId(), clock.instant()));
    }

    @Override
    public boolean disallow(DisallowGuildCommand command) {
        requireOwner(command.requester());
        GuildId guildId = command.guildId();
        boolean removed = allowedGuildPort.delete(guildId);
        if (!currentAllowlist().permits(guildId)) {
            joinedGuildPort.leaveWithNotice(guildId);
        }
        return removed;
    }

    private GuildAllowlist currentAllowlist() {
        return new GuildAllowlist(guildAllowlistSettingPort.isEnabled(),
                allowedGuildPort.findAll().stream().map(AllowedGuild::guildId).collect(Collectors.toSet()));
    }

    private void requireOwner(DiscordUserId requester) {
        if (!botOwnerPort.owner().equals(requester)) {
            throw new NotBotOwnerException(requester);
        }
    }
}
