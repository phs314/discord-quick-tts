package io.github.phs314.quicktts.bot.device.application.service;

import io.github.phs314.quicktts.bot.device.application.InvalidPairingCodeException;
import io.github.phs314.quicktts.bot.device.application.port.in.AuthenticateDeviceUseCase;
import io.github.phs314.quicktts.bot.device.application.port.in.IssuePairingCodeUseCase;
import io.github.phs314.quicktts.bot.device.application.port.in.RegisterDeviceUseCase;
import io.github.phs314.quicktts.bot.device.application.port.out.DeviceRepository;
import io.github.phs314.quicktts.bot.device.application.port.out.PairingRepository;
import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.DeviceToken;
import io.github.phs314.quicktts.bot.device.domain.Pairing;
import io.github.phs314.quicktts.bot.device.domain.PairingCode;
import io.github.phs314.quicktts.bot.shared.domain.DiscordUserId;
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
    public Optional<DiscordUserId> authenticate(String rawDeviceToken) {
        if (rawDeviceToken == null || rawDeviceToken.isBlank()) {
            return Optional.empty();
        }
        String tokenHash = new DeviceToken(rawDeviceToken).hash();
        return deviceRepository.findByTokenHash(tokenHash).map(Device::owner);
    }
}
