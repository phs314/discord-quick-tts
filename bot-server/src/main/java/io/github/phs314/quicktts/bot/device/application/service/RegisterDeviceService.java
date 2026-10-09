package io.github.phs314.quicktts.bot.device.application.service;

import io.github.phs314.quicktts.bot.device.application.exception.InvalidPairingCodeException;
import io.github.phs314.quicktts.bot.device.application.port.in.usecase.RegisterDeviceUseCase;
import io.github.phs314.quicktts.bot.device.application.port.out.DevicePort;
import io.github.phs314.quicktts.bot.device.application.port.out.PairingPort;
import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.Pairing;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.device.domain.vo.PairingCode;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterDeviceService implements RegisterDeviceUseCase {

    private final PairingPort pairingPort;
    private final DevicePort devicePort;
    private final Clock clock;

    @Override
    public DeviceToken register(PairingCode code, DeviceName name) {
        Instant now = clock.instant();
        Pairing pairing = pairingPort.take(code)
                .filter(found -> !found.isExpired(now))
                .orElseThrow(InvalidPairingCodeException::new);

        DeviceToken token = DeviceToken.generate();
        devicePort.save(Device.register(token, pairing.owner(), name, now));
        return token;
    }
}
