package io.github.phs314.quicktts.bot.device.application.service;

import io.github.phs314.quicktts.bot.device.application.port.in.AuthenticateDeviceUseCase;
import io.github.phs314.quicktts.bot.device.application.port.out.DevicePort;
import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.util.Optional;

public class AuthenticateDeviceService implements AuthenticateDeviceUseCase {

    private final DevicePort devicePort;

    public AuthenticateDeviceService(DevicePort devicePort) {
        this.devicePort = devicePort;
    }

    @Override
    public Optional<DiscordUserId> authenticate(String rawDeviceToken) {
        if (rawDeviceToken == null || rawDeviceToken.isBlank()) {
            return Optional.empty();
        }
        String tokenHash = new DeviceToken(rawDeviceToken).hash();
        return devicePort.findByTokenHash(tokenHash).map(Device::owner);
    }
}
