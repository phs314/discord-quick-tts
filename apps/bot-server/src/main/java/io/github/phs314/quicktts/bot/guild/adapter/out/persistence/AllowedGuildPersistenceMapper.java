package io.github.phs314.quicktts.bot.guild.adapter.out.persistence;

import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;

/**
 * {@link AllowedGuild} 애그리거트와 {@link AllowedGuildJpaEntity} 를 서로 바꾼다 (ADR 0038).
 */
final class AllowedGuildPersistenceMapper {

    private AllowedGuildPersistenceMapper() {
    }

    static AllowedGuildJpaEntity toEntity(AllowedGuild allowedGuild) {
        return new AllowedGuildJpaEntity(allowedGuild.guildId().value(), allowedGuild.allowedAt());
    }

    static AllowedGuild toDomain(AllowedGuildJpaEntity entity) {
        return AllowedGuild.restore(new GuildId(entity.getGuildId()), entity.getAllowedAt());
    }
}
