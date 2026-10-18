package io.github.phs314.quicktts.bot.speech.adapter.out.persistence;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;

/**
 * 사용자와 고른 목소리(값 객체)를 {@link VoicePreferenceJpaEntity} 와 서로 바꾼다 (ADR 0038).
 */
final class VoicePreferencePersistenceMapper {

    private VoicePreferencePersistenceMapper() {
    }

    static VoicePreferenceJpaEntity toEntity(DiscordUserId user, VoiceId voiceId) {
        return new VoicePreferenceJpaEntity(user.value(), voiceId.value());
    }

    static VoiceId toVoiceId(VoicePreferenceJpaEntity entity) {
        return new VoiceId(entity.getVoiceId());
    }
}
