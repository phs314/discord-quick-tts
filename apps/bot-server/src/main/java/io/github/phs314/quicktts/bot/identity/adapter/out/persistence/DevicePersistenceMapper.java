package io.github.phs314.quicktts.bot.identity.adapter.out.persistence;

import io.github.phs314.quicktts.bot.identity.domain.Device;
import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceTokenHash;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;

/**
 * {@link Device} 애그리거트와 {@link DeviceJpaEntity} 를 서로 바꾼다 (ADR 0038).
 */
final class DevicePersistenceMapper {

    private DevicePersistenceMapper() {
    }

    static DeviceJpaEntity toEntity(Device device) {
        return new DeviceJpaEntity(device.id().value(), device.tokenHash().value(), device.userId().value(),
                device.name().value(), device.registeredAt());
    }

    static Device toDomain(DeviceJpaEntity entity) {
        return Device.restore(
                new DeviceId(entity.getId()),
                new DeviceTokenHash(entity.getTokenHash()),
                new DiscordUserId(entity.getDiscordUserId()),
                new DeviceName(entity.getName()),
                entity.getRegisteredAt());
    }
}
