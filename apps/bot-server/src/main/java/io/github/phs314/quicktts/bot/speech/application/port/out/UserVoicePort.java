package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;

/**
 * 사용자의 문장을 읽을 목소리. 사용자가 고른 목소리는 사용자 설정(settings)이 안다.
 */
public interface UserVoicePort {

    /** 고른 목소리. 고른 적이 없거나 더 이상 없는 목소리면 기본 목소리. */
    VoiceId getVoiceId(DiscordUserId user);
}
