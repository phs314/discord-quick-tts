package io.github.phs314.quicktts.bot.identity.application.service;

import io.github.phs314.quicktts.bot.identity.application.port.in.IssuePairingCodeUseCase;
import io.github.phs314.quicktts.bot.identity.application.port.in.command.IssuePairingCodeCommand;
import io.github.phs314.quicktts.bot.identity.application.port.in.dto.IssuedPairingCodeDto;
import io.github.phs314.quicktts.bot.identity.application.port.out.PairingPort;
import io.github.phs314.quicktts.bot.identity.domain.Pairing;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssuePairingCodeService implements IssuePairingCodeUseCase {

    private final PairingPort pairingPort;
    private final Clock clock;

    @Override
    public IssuedPairingCodeDto issue(IssuePairingCodeCommand command) {
        Pairing pairing = Pairing.issue(command.userId(), clock.instant());
        pairingPort.save(pairing);
        return IssuedPairingCodeDto.from(pairing);
    }
}
