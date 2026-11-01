package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.in.VacateBotSeatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.VacateBotSeatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.out.BotSeatPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VacateBotSeatService implements VacateBotSeatUseCase {

    private final BotSeatPort botSeatPort;
    private final SpeechPlayerPort speechPlayerPort;

    @Override
    public void vacate(VacateBotSeatCommand command) {
        GuildId guildId = command.guildId();
        botSeatPort.withLockedSeat(guildId, seat -> {
            seat.vacate();
            speechPlayerPort.forget(guildId);
        });
    }
}
