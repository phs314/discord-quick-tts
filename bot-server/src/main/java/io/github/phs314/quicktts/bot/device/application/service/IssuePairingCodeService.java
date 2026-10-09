package io.github.phs314.quicktts.bot.device.application.service;

import io.github.phs314.quicktts.bot.device.application.port.in.IssuePairingCodeUseCase;
import io.github.phs314.quicktts.bot.device.application.port.out.PairingPort;
import io.github.phs314.quicktts.bot.device.domain.Pairing;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Clock;

public class IssuePairingCodeService implements IssuePairingCodeUseCase {

    private final PairingPort pairingPort;
    private final Clock clock;

    public IssuePairingCodeService(PairingPort pairingPort, Clock clock) {
        this.pairingPort = pairingPort;
        this.clock = clock;
    }

    @Override
    public Pairing issue(DiscordUserId owner) {
        Pairing pairing = Pairing.issue(owner, clock.instant());
        pairingPort.save(pairing);
        return pairing;
    }
}
