package io.github.phs314.quicktts.bot.identity.application.service;

import io.github.phs314.quicktts.bot.identity.application.port.in.RegisterDeviceUseCase;
import io.github.phs314.quicktts.bot.identity.application.port.in.command.RegisterDeviceCommand;
import io.github.phs314.quicktts.bot.identity.application.port.out.DevicePort;
import io.github.phs314.quicktts.bot.identity.application.port.out.PairingPort;
import io.github.phs314.quicktts.bot.identity.domain.Device;
import io.github.phs314.quicktts.bot.identity.domain.InvalidPairingCodeException;
import io.github.phs314.quicktts.bot.identity.domain.Pairing;
import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterDeviceService implements RegisterDeviceUseCase {

    private final PairingPort pairingPort;
    private final DevicePort devicePort;
    private final Clock clock;

    @Override
    @Transactional
    public DeviceToken register(RegisterDeviceCommand command) {
        Instant now = clock.instant();
        Pairing pairing = pairingPort.findAndDelete(command.code()).orElseThrow(InvalidPairingCodeException::new);
        DiscordUserId userId = pairing.redeem(now);

        DeviceToken token = DeviceToken.generate();
        devicePort.save(Device.register(token, userId, command.name(), now));
        return token;
    }
}
