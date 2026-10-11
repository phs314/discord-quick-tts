package io.github.phs314.quicktts.bot.guild.application.service;

import io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException;
import io.github.phs314.quicktts.bot.guild.application.port.in.dto.AllowedGuildDto;
import io.github.phs314.quicktts.bot.guild.application.port.in.dto.GuildAllowlistDto;
import io.github.phs314.quicktts.bot.guild.application.port.in.usecase.ManageGuildAllowlistUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.BotOwnerPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.GuildAllowlistSettingPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.JoinedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManageGuildAllowlistService implements ManageGuildAllowlistUseCase {

    private final BotOwnerPort botOwner;
    private final AllowedGuildPort allowedGuilds;
    private final JoinedGuildPort joinedGuilds;
    private final GuildAllowlistSettingPort setting;
    private final Clock clock;

    @Override
    public GuildAllowlistDto allowlist(DiscordUserId requester) {
        requireOwner(requester);
        return new GuildAllowlistDto(setting.enabled(),
                allowedGuilds.findAll().stream().map(AllowedGuildDto::from).toList());
    }

    @Override
    public boolean allow(DiscordUserId requester, GuildId guild) {
        requireOwner(requester);
        return allowedGuilds.add(AllowedGuild.allow(guild, clock.instant()));
    }

    @Override
    public boolean disallow(DiscordUserId requester, GuildId guild) {
        requireOwner(requester);
        boolean removed = allowedGuilds.delete(guild);
        if (setting.enabled() && joinedGuilds.joinedGuilds().contains(guild)) {
            joinedGuilds.leaveNotAllowed(guild);
        }
        return removed;
    }

    private void requireOwner(DiscordUserId requester) {
        if (!botOwner.owner().equals(requester)) {
            throw new NotBotOwnerException(requester);
        }
    }
}
