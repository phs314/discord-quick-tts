package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.port.in.FindCurrentUserVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.dto.VoiceChannelDto;
import io.github.phs314.quicktts.bot.speech.application.port.in.query.FindCurrentUserVoiceChannelQuery;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceStatePort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindCurrentUserVoiceChannelService implements FindCurrentUserVoiceChannelUseCase {

    private final VoiceStatePort voiceStatePort;

    @Override
    public Optional<VoiceChannelDto> findCurrentUserVoiceChannel(FindCurrentUserVoiceChannelQuery query) {
        return voiceStatePort.findUserVoiceChannel(query.user());
    }
}
