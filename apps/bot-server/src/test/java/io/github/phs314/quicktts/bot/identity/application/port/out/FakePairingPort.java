package io.github.phs314.quicktts.bot.identity.application.port.out;

import io.github.phs314.quicktts.bot.identity.domain.Pairing;
import io.github.phs314.quicktts.bot.identity.domain.vo.PairingCode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** 연결 코드 저장소 대신 쓰는 메모리 대역. */
public class FakePairingPort implements PairingPort {

    private final Map<PairingCode, Pairing> byCode = new HashMap<>();

    /** 아직 쓰지 않은 연결 코드들. */
    public Map<PairingCode, Pairing> saved() {
        return Map.copyOf(byCode);
    }

    @Override
    public void save(Pairing pairing) {
        byCode.put(pairing.code(), pairing);
    }

    @Override
    public Optional<Pairing> findAndDelete(PairingCode code) {
        return Optional.ofNullable(byCode.remove(code));
    }
}
