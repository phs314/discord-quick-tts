package io.github.phs314.quicktts.bot.identity.adapter.out.persistence;

import io.github.phs314.quicktts.bot.identity.domain.Pairing;
import io.github.phs314.quicktts.bot.identity.domain.vo.PairingCode;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;

/**
 * {@link Pairing} 과 {@link PairingJpaEntity} 를 서로 바꾼다 (ADR 0038).
 */
final class PairingPersistenceMapper {

    private PairingPersistenceMapper() {
    }

    static PairingJpaEntity toEntity(Pairing pairing) {
        return new PairingJpaEntity(pairing.code().value(), pairing.userId().value(), pairing.expiresAt());
    }

    static Pairing toDomain(PairingJpaEntity entity) {
        return Pairing.restore(new PairingCode(entity.getCode()), new DiscordUserId(entity.getDiscordUserId()),
                entity.getExpiresAt());
    }
}
