package io.github.phs314.quicktts.bot.device.adapter.out.persistence;

import io.github.phs314.quicktts.bot.device.application.port.out.PairingRepository;
import io.github.phs314.quicktts.bot.device.domain.Pairing;
import io.github.phs314.quicktts.bot.device.domain.PairingCode;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 연결 코드는 몇 분만 살아 있으면 되므로 메모리에만 둔다. 서버를 재시작하면 사라진다.
 */
@Component
public class InMemoryPairingRepository implements PairingRepository {

    private final Map<PairingCode, Pairing> pairings = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryPairingRepository(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void save(Pairing pairing) {
        pairings.values().removeIf(existing -> existing.isExpired(clock.instant()));
        pairings.put(pairing.code(), pairing);
    }

    @Override
    public Optional<Pairing> take(PairingCode code) {
        return Optional.ofNullable(pairings.remove(code));
    }
}
