package io.github.phs314.quicktts.bot.identity.adapter.out.persistence;

import io.github.phs314.quicktts.bot.identity.application.port.out.PairingPort;
import io.github.phs314.quicktts.bot.identity.domain.Pairing;
import io.github.phs314.quicktts.bot.identity.domain.vo.PairingCode;
import java.time.Clock;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 발급한 연결 코드를 {@code pairing} 테이블에 저장한다. 봇 서버를 다시 켜도, 봇과 API 를 다른 프로세스로 나눠도 코드가 남는다.
 * 스키마는 {@code db/migration} 에 있다.
 */
@Component
@RequiredArgsConstructor
public class PairingPersistenceAdapter implements PairingPort {

    private final PairingSpringDataRepository pairingSpringDataRepository;
    private final Clock clock;

    /** 저장할 때 만료된 코드를 함께 지워서 테이블이 쌓이지 않게 한다. */
    @Override
    public void save(Pairing pairing) {
        pairingSpringDataRepository.deleteExpired(clock.instant());
        pairingSpringDataRepository.save(PairingPersistenceMapper.toEntity(pairing));
    }

    /** 읽은 뒤 지워서, 지운 줄이 있을 때만 돌려준다. 같은 코드를 동시에 써도 한 번만 꺼내진다. */
    @Override
    public Optional<Pairing> findAndDelete(PairingCode code) {
        return pairingSpringDataRepository.findById(code.value())
                .filter(entity -> pairingSpringDataRepository.deleteByCode(code.value()) == 1)
                .map(PairingPersistenceMapper::toDomain);
    }
}
