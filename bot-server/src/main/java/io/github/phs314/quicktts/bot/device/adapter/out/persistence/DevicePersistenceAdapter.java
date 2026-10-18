package io.github.phs314.quicktts.bot.device.adapter.out.persistence;

import io.github.phs314.quicktts.bot.device.application.port.out.DevicePort;
import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceTokenHash;
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
        return deviceSpringDataRepository.findById(tokenHash.value())
                .map(DevicePersistenceMapper::toDomain);
    }

    @Override
    public List<Device> findByOwner(DiscordUserId owner) {
        return deviceSpringDataRepository.findByDiscordUserIdOrderByRegisteredAt(owner.value()).stream()
                .map(DevicePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Device> findById(DeviceId id) {
        return deviceSpringDataRepository.findByDeviceId(id.value())
                .map(DevicePersistenceMapper::toDomain);
    }

    @Override
    public boolean delete(DeviceId id) {
        return deviceSpringDataRepository.deleteByDeviceId(id.value()) > 0;
    }

    @Override
    public int deleteAllByOwner(DiscordUserId owner) {
        return deviceSpringDataRepository.deleteByDiscordUserId(owner.value());
    }
}
