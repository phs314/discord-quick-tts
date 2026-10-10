package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.LeaveEmptyVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.BotSeatPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveEmptyVoiceChannelService implements LeaveEmptyVoiceChannelUseCase {

    private final BotSeatPort botSeats;
    private final VoiceChannelLocatorPort voiceChannelLocator;
    private final SpeechPlayerPort speechPlayer;

    @Override
    public void leaveIfEmpty(GuildId guildId) {
        botSeats.withSeat(guildId, seat -> {
            if (seat.vacateIfEmpty(voiceChannelLocator::hasPeople)) {
                speechPlayer.leave(guildId);
            }
        });
    }
}
