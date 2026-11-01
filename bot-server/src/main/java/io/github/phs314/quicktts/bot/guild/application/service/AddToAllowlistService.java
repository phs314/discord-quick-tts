package io.github.phs314.quicktts.bot.guild.application.service;

import io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException;
import io.github.phs314.quicktts.bot.guild.application.port.in.AddToAllowlistUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.AddToAllowlistCommand;
import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.BotOwnerPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddToAllowlistService implements AddToAllowlistUseCase {

    private final BotOwnerPort botOwnerPort;
    private final AllowedGuildPort allowedGuildPort;
    private final Clock clock;

    @Override
    @Transactional
    public boolean add(AddToAllowlistCommand command) {
        requireOwner(command.requester());
        return allowedGuildPort.saveIfAbsent(AllowedGuild.allow(command.guildId(), clock.instant()));
    }

    private void requireOwner(DiscordUserId requester) {
        if (!botOwnerPort.owner().equals(requester)) {
            throw new NotBotOwnerException(requester);
        }
    }
}
