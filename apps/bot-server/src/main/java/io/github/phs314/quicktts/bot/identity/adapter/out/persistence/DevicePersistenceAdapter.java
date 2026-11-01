package io.github.phs314.quicktts.bot.identity.adapter.out.persistence;

import io.github.phs314.quicktts.bot.identity.application.port.out.DevicePort;
import io.github.phs314.quicktts.bot.identity.domain.Device;
import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceTokenHash;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 등록된 기기를 {@code device} 테이블에 저장한다. 스키마는 {@code db/migration} 에 있다.
 */
@Component
@RequiredArgsConstructor
public class DevicePersistenceAdapter implements DevicePort {

    private final DeviceSpringDataRepository deviceSpringDataRepository;

    @Override
    public void save(Device device) {
        deviceSpringDataRepository.save(DevicePersistenceMapper.toEntity(device));
    }

    @Override
    public Optional<Device> findByTokenHash(DeviceTokenHash tokenHash) {
        return deviceSpringDataRepository.findByTokenHash(tokenHash.value())
                .map(DevicePersistenceMapper::toDomain);
    }

    @Override
    public List<Device> findByUserId(DiscordUserId userId) {
        return deviceSpringDataRepository.findByDiscordUserIdOrderByRegisteredAt(userId.value()).stream()
                .map(DevicePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Device> findById(DeviceId id) {
        return deviceSpringDataRepository.findById(id.value())
                .map(DevicePersistenceMapper::toDomain);
    }

    @Override
    public boolean delete(DeviceId id) {
        return deviceSpringDataRepository.deleteDeviceById(id.value()) > 0;
    }

    @Override
    public int deleteAllByUserId(DiscordUserId userId) {
        return deviceSpringDataRepository.deleteByDiscordUserId(userId.value());
    }
}
