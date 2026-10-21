package io.github.phs314.quicktts.bot.device.adapter.out.memory;

import io.github.phs314.quicktts.bot.device.application.port.out.PairingPort;
import io.github.phs314.quicktts.bot.device.domain.Pairing;
import io.github.phs314.quicktts.bot.device.domain.vo.PairingCode;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 연결 코드는 몇 분만 살아 있으면 되므로 메모리에만 둔다. 서버를 재시작하면 사라진다.
 */
@Component
public class PairingMemoryAdapter implements PairingPort {

    private final Map<PairingCode, Pairing> pairings = new ConcurrentHashMap<>();
    private final Clock clock;

    // 초기화한 final 필드가 있으면 VS Code 의 Lombok 이 @RequiredArgsConstructor 를 잘못 읽어 오류를 띄우므로 직접 쓴다.
    public PairingMemoryAdapter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void save(Pairing pairing) {
        pairings.values().removeIf(existing -> existing.isExpired(clock.instant()));
        pairings.put(pairing.code(), pairing);
    }

    @Override
    public Optional<Pairing> findAndDelete(PairingCode code) {
        return Optional.ofNullable(pairings.remove(code));
    }
}
