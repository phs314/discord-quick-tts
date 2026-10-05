package io.github.phs314.quicktts.bot.device.application.port.out;

import io.github.phs314.quicktts.bot.device.domain.Device;
import java.util.Optional;

public interface DeviceRepository {

    void save(Device device);

    Optional<Device> findByTokenHash(String tokenHash);
}
