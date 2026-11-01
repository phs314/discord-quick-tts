package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.util.Optional;

/**
 * 사용자가 고른 목소리를 기억한다.
 */
public interface VoicePreferencePort {

    Optional<VoiceId> findVoice(DiscordUserId user);

    void saveVoice(DiscordUserId user, VoiceId voiceId);
}
