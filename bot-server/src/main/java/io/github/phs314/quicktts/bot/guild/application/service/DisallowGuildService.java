package io.github.phs314.quicktts.bot.guild.application.service;

import io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException;
import io.github.phs314.quicktts.bot.guild.application.port.in.DisallowGuildUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.DisallowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.BotOwnerPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.GuildAllowlistEventPort;
import io.github.phs314.quicktts.bot.guild.domain.event.GuildDisallowed;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DisallowGuildService implements DisallowGuildUseCase {

    private final BotOwnerPort botOwnerPort;
    private final AllowedGuildPort allowedGuildPort;
    private final GuildAllowlistEventPort guildAllowlistEventPort;

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
