package io.github.phs314.quicktts.bot.device.application.service;

import io.github.phs314.quicktts.bot.device.application.InvalidPairingCodeException;
import io.github.phs314.quicktts.bot.device.application.port.in.AuthenticateDeviceUseCase;
import io.github.phs314.quicktts.bot.device.application.port.in.IssuePairingCodeUseCase;
import io.github.phs314.quicktts.bot.device.application.port.in.ManageOwnDevicesUseCase;
import io.github.phs314.quicktts.bot.device.application.port.in.RegisterDeviceUseCase;
import io.github.phs314.quicktts.bot.device.application.port.out.DevicePort;
import io.github.phs314.quicktts.bot.device.application.port.out.PairingPort;
import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.device.domain.Pairing;
import io.github.phs314.quicktts.bot.device.domain.vo.PairingCode;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class DeviceRegistrationService implements
        IssuePairingCodeUseCase, RegisterDeviceUseCase, AuthenticateDeviceUseCase, ManageOwnDevicesUseCase {

    private final PairingPort pairingPort;
    private final DevicePort devicePort;
    private final Clock clock;

    public DeviceRegistrationService(PairingPort pairingPort,
                                     DevicePort devicePort,
                                     Clock clock) {
        this.pairingPort = pairingPort;
        this.devicePort = devicePort;
        this.clock = clock;
    }

    @Override
    public Pairing issue(DiscordUserId owner) {
        Pairing pairing = Pairing.issue(owner, clock.instant());
        pairingPort.save(pairing);
        return pairing;
    }

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

    @Override
    public Optional<DiscordUserId> authenticate(String rawDeviceToken) {
        if (rawDeviceToken == null || rawDeviceToken.isBlank()) {
            return Optional.empty();
        }
        String tokenHash = new DeviceToken(rawDeviceToken).hash();
        return devicePort.findByTokenHash(tokenHash).map(Device::owner);
    }

    @Override
    public List<Device> listDevices(DiscordUserId owner) {
        return devicePort.findByOwner(owner);
    }

    @Override
    public boolean unlink(DiscordUserId owner, DeviceId id) {
        return devicePort.deleteByIdAndOwner(id, owner);
    }

    @Override
    public int unlinkAll(DiscordUserId owner) {
        return devicePort.deleteAllByOwner(owner);
    }
}
