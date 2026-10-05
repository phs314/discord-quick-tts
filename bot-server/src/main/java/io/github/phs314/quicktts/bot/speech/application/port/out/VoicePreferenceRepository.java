package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.VoiceId;
import java.util.Optional;

/**
 * 사용자가 고른 목소리를 기억한다.
 */
public interface VoicePreferenceRepository {

    Optional<VoiceId> find(DiscordUserId user);

    void save(DiscordUserId user, VoiceId voice);
}
