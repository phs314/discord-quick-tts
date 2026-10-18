package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.port.in.query.FindMyVoiceChannelQuery;
import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.FindMyVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindMyVoiceChannelService implements FindMyVoiceChannelUseCase {

    private final VoiceChannelLocatorPort voiceChannelLocatorPort;

    @Override
    public Optional<VoiceChannelDetails> findMyVoiceChannel(FindMyVoiceChannelQuery query) {
        return voiceChannelLocatorPort.findCurrentChannel(query.user());
    }
}
