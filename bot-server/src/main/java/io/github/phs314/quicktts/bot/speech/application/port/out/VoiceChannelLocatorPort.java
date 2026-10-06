package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import java.util.Optional;

/**
 * 사용자가 지금 들어가 있는 음성 채널을 찾는다. 봇이 들어가 있는 서버의 채널만 찾을 수 있다.
 */
public interface VoiceChannelLocatorPort {

    Optional<VoiceChannelDetails> findCurrentChannel(DiscordUserId user);
}
