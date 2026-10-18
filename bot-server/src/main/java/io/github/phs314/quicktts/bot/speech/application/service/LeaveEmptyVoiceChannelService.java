package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.LeaveIfEmptyCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.LeaveEmptyVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.BotSeatPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveEmptyVoiceChannelService implements LeaveEmptyVoiceChannelUseCase {

    private final BotSeatPort botSeatPort;
    private final VoiceChannelLocatorPort voiceChannelLocatorPort;
    private final SpeechPlayerPort speechPlayerPort;

    @Override
    public void leaveIfEmpty(LeaveIfEmptyCommand command) {
        GuildId guildId = command.guildId();
        botSeatPort.withLockedSeat(guildId, seat -> {
            if (seat.vacateIfEmpty(voiceChannelLocatorPort::hasPeople)) {
                speechPlayerPort.leave(guildId);
            }
        });
    }
}
