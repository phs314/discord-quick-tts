package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import java.util.Optional;
import java.util.function.Function;

/**
 * @param speakers        사용자가 지금 있는 음성 채널
 * @param botChannelInUse 디스코드 서버에서 봇이 쓰이고 있는 음성 채널
 */
record FakeVoiceChannelLocator(Function<DiscordUserId, Optional<VoiceChannelDetails>> speakers,
                               Function<GuildId, Optional<VoiceChannelDetails>> botChannelInUse)
        implements VoiceChannelLocatorPort {

    @Override
    public Optional<VoiceChannelDetails> findCurrentChannel(DiscordUserId user) {
        return speakers.apply(user);
    }

    @Override
    public Optional<VoiceChannelDetails> findBotChannelInUse(GuildId guildId) {
        return botChannelInUse.apply(guildId);
    }
}
