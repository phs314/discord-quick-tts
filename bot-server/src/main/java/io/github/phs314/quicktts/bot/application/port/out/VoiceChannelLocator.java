package io.github.phs314.quicktts.bot.application.port.out;

import io.github.phs314.quicktts.bot.domain.DiscordUserId;
import io.github.phs314.quicktts.bot.domain.VoiceChannel;
import java.util.Optional;

/**
 * 사용자가 지금 들어가 있는 음성 채널을 찾는다.
 */
public interface VoiceChannelLocator {

    Optional<VoiceChannel> findCurrentChannel(DiscordUserId user);
}
