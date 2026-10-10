package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import java.util.Optional;

/**
 * 디스코드의 음성 채널 상태를 본다. 봇이 들어가 있는 서버의 채널만 볼 수 있다.
 */
public interface VoiceChannelLocatorPort {

    /** 사용자가 지금 들어가 있는 음성 채널. */
    Optional<VoiceChannelDetails> findCurrentChannel(DiscordUserId user);

    /** 사용자에게 보여 줄 음성 채널 정보. 채널이 없어졌으면 빈 값이다. */
    Optional<VoiceChannelDetails> findDetails(VoiceChannel channel);

    /** 음성 채널에 봇 말고 사람이 한 명이라도 있는지. 채널이 없어졌으면 false 다. */
    boolean hasPeople(VoiceChannel channel);
}
