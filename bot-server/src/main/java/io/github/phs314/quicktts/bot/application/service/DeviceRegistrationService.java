package io.github.phs314.quicktts.bot.application.service;

import io.github.phs314.quicktts.bot.application.InvalidPairingCodeException;
import io.github.phs314.quicktts.bot.application.port.in.AuthenticateDeviceUseCase;
import io.github.phs314.quicktts.bot.application.port.in.IssuePairingCodeUseCase;
import io.github.phs314.quicktts.bot.application.port.in.RegisterDeviceUseCase;
import io.github.phs314.quicktts.bot.application.port.out.DeviceRepository;
import io.github.phs314.quicktts.bot.application.port.out.PairingRepository;
import io.github.phs314.quicktts.bot.domain.DiscordUserId;
import io.github.phs314.quicktts.bot.domain.device.Device;
import io.github.phs314.quicktts.bot.domain.device.DeviceToken;
import io.github.phs314.quicktts.bot.domain.device.Pairing;
import io.github.phs314.quicktts.bot.domain.device.PairingCode;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

public class DeviceRegistrationService
        implements IssuePairingCodeUseCase, RegisterDeviceUseCase, AuthenticateDeviceUseCase {

    private final PairingRepository pairingRepository;
    private final DeviceRepository deviceRepository;
    private final Clock clock;

    public DeviceRegistrationService(PairingRepository pairingRepository,
                                     DeviceRepository deviceRepository,
                                     Clock clock) {
        this.pairingRepository = pairingRepository;
        this.deviceRepository = deviceRepository;
        this.clock = clock;
    }

    @Override
    public Pairing issue(DiscordUserId owner) {
        Pairing pairing = Pairing.issue(owner, clock.instant());
        pairingRepository.save(pairing);
        return pairing;
    }

    @Override
    public DeviceToken register(PairingCode code) {
        Instant now = clock.instant();
        Pairing pairing = pairingRepository.take(code)
                .filter(found -> !found.isExpired(now))
                .orElseThrow(InvalidPairingCodeException::new);

        DeviceToken token = DeviceToken.generate();
        deviceRepository.save(new Device(token.hash(), pairing.owner(), now));
        return token;
    }

    @Override
    public Optional<DiscordUserId> authenticate(DeviceToken token) {
        return deviceRepository.findByTokenHash(token.hash()).map(Device::owner);
    }
}
