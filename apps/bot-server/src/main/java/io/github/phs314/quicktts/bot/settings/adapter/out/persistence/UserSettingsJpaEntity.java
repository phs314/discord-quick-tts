package io.github.phs314.quicktts.bot.settings.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * {@code user_settings} 테이블의 한 줄. {@link UserSettingsPersistenceMapper} 가 도메인과 옮긴다 (ADR 0038).
 */
@Entity
@Table(name = "user_settings")
class UserSettingsJpaEntity {

    @Id
    @Column(name = "discord_user_id")
    private long discordUserId;

    @Column(name = "voice_id")
    private String voiceId;

    protected UserSettingsJpaEntity() {
    }

    UserSettingsJpaEntity(long discordUserId, String voiceId) {
        this.discordUserId = discordUserId;
        this.voiceId = voiceId;
    }

    long getDiscordUserId() {
        return discordUserId;
    }

    String getVoiceId() {
        return voiceId;
    }
}
