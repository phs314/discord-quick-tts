package io.github.phs314.quicktts.bot.settings.adapter.out.persistence;

import io.github.phs314.quicktts.bot.settings.domain.UserSettings;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;

/**
 * {@link UserSettings} 와 {@link UserSettingsJpaEntity} 를 서로 바꾼다 (ADR 0038).
 */
final class UserSettingsPersistenceMapper {

    private UserSettingsPersistenceMapper() {
    }

    static UserSettingsJpaEntity toEntity(UserSettings settings) {
        return new UserSettingsJpaEntity(settings.userId().value(), settings.voiceId().map(VoiceId::value).orElse(null));
    }

    static UserSettings toDomain(UserSettingsJpaEntity entity) {
        return new UserSettings(new DiscordUserId(entity.getDiscordUserId()),
                entity.getVoiceId() == null ? null : new VoiceId(entity.getVoiceId()));
    }
}
