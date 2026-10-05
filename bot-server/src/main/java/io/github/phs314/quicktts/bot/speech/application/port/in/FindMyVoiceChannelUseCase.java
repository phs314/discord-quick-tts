package io.github.phs314.quicktts.bot.speech.application.port.in;

import io.github.phs314.quicktts.bot.shared.domain.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.VoiceChannelDetails;
import java.util.Optional;

/**
 * 사용자가 quick chat 을 보내면 어느 음성 채널에서 읽힐지 미리 알려 준다.
 */
public interface FindMyVoiceChannelUseCase {

    /** 봇이 있는 서버의 음성 채널에 없으면 비어 있다. */
    Optional<VoiceChannelDetails> findMyVoiceChannel(DiscordUserId user);
}
