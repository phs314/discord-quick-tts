package io.github.phs314.quicktts.bot.identity.application.service;

import io.github.phs314.quicktts.bot.identity.application.port.in.AuthenticateDeviceUseCase;
import io.github.phs314.quicktts.bot.identity.application.port.in.query.AuthenticateDeviceQuery;
import io.github.phs314.quicktts.bot.identity.application.port.out.DevicePort;
import io.github.phs314.quicktts.bot.identity.domain.Device;
import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceTokenHash;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticateDeviceService implements AuthenticateDeviceUseCase {

    private final DevicePort devicePort;

    @Override
    public Optional<DiscordUserId> authenticate(AuthenticateDeviceQuery query) {
        String rawDeviceToken = query.rawDeviceToken();
        if (rawDeviceToken == null || rawDeviceToken.isBlank()) {
            return Optional.empty();
        }
        DeviceTokenHash tokenHash = new DeviceToken(rawDeviceToken).hash();
        return devicePort.findByTokenHash(tokenHash).map(Device::userId);
    }
}
