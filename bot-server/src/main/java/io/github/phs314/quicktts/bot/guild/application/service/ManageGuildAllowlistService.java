package io.github.phs314.quicktts.bot.guild.application.service;

import io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException;
import io.github.phs314.quicktts.bot.guild.application.port.in.ManageGuildAllowlistUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.AllowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.DisallowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.in.dto.AllowedGuildDto;
import io.github.phs314.quicktts.bot.guild.application.port.in.dto.GuildAllowlistDto;
import io.github.phs314.quicktts.bot.guild.application.port.in.query.GetAllowlistQuery;
import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.BotOwnerPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.GuildAllowlistEventPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.GuildAllowlistSettingPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.guild.domain.event.GuildDisallowed;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManageGuildAllowlistService implements ManageGuildAllowlistUseCase {

    private final BotOwnerPort botOwnerPort;
    private final AllowedGuildPort allowedGuildPort;
    private final GuildAllowlistSettingPort guildAllowlistSettingPort;
    private final GuildAllowlistEventPort guildAllowlistEventPort;
    private final Clock clock;

    @Override
    public GuildAllowlistDto getAllowlist(GetAllowlistQuery query) {
        requireOwner(query.requester());
        return new GuildAllowlistDto(guildAllowlistSettingPort.isEnabled(),
                allowedGuildPort.findAll().stream().map(AllowedGuildDto::from).toList());
    }

    @Override
    @Transactional
    public boolean allow(AllowGuildCommand command) {
        requireOwner(command.requester());
        return allowedGuildPort.saveIfAbsent(AllowedGuild.allow(command.guildId(), clock.instant()));
    }

    /** 목록에서 빼기만 하고, 봇이 그 서버에서 나가는 일은 커밋 뒤에 이벤트를 받는 쪽이 한다 (ADR 0033). */
    @Override
    @Transactional
    public boolean disallow(DisallowGuildCommand command) {
        requireOwner(command.requester());
        boolean removed = allowedGuildPort.delete(command.guildId());
        if (removed) {
            guildAllowlistEventPort.publish(new GuildDisallowed(command.guildId()));
        }
        return removed;
    }

    private void requireOwner(DiscordUserId requester) {
        if (!botOwnerPort.owner().equals(requester)) {
            throw new NotBotOwnerException(requester);
        }
    }
}
