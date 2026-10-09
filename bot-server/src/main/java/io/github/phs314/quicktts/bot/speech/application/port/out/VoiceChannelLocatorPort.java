package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import java.util.Optional;

/**
 * 사용자나 봇이 지금 들어가 있는 음성 채널을 찾는다. 봇이 들어가 있는 서버의 채널만 찾을 수 있다.
 */
public interface VoiceChannelLocatorPort {

    Optional<VoiceChannelDetails> findCurrentChannel(DiscordUserId user);

    /**
     * 봇이 그 디스코드 서버에서 쓰이고 있는 음성 채널. 봇이 들어가 있고 봇 말고 사람이 한 명이라도 남아 있는 채널이다.
     * 봇이 어느 채널에도 없거나 있는 채널에 사람이 없으면 빈 값이다.
     */
    Optional<VoiceChannelDetails> findBotChannelInUse(GuildId guildId);
}
