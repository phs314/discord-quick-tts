package io.github.phs314.quicktts.bot.speech.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * {@code voice_preference} 테이블의 한 줄. {@link VoicePreferencePersistenceMapper} 가 값 객체와 옮긴다 (ADR 0038).
 */
@Entity
@Table(name = "voice_preference")
class VoicePreferenceJpaEntity {

    @Id
    @Column(name = "discord_user_id")
    private long discordUserId;

    @Column(name = "voice_id")
    private String voiceId;

    protected VoicePreferenceJpaEntity() {
    }

    VoicePreferenceJpaEntity(long discordUserId, String voiceId) {
        this.discordUserId = discordUserId;
        this.voiceId = voiceId;
    }

    String getVoiceId() {
        return voiceId;
    }
}
