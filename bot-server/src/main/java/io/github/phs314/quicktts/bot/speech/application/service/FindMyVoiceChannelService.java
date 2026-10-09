package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.in.FindMyVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import java.util.Optional;

public class FindMyVoiceChannelService implements FindMyVoiceChannelUseCase {

    private final VoiceChannelLocatorPort voiceChannelLocator;

    public FindMyVoiceChannelService(VoiceChannelLocatorPort voiceChannelLocator) {
        this.voiceChannelLocator = voiceChannelLocator;
    }

    @Override
    public Optional<VoiceChannelDetails> findMyVoiceChannel(DiscordUserId user) {
        return voiceChannelLocator.findCurrentChannel(user);
    }
}
