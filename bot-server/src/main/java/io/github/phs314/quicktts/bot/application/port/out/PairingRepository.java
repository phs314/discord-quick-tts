package io.github.phs314.quicktts.bot.application.port.out;

import io.github.phs314.quicktts.bot.domain.device.Pairing;
import io.github.phs314.quicktts.bot.domain.device.PairingCode;
import java.util.Optional;

public interface PairingRepository {

    void save(Pairing pairing);

    /** 코드를 꺼내면서 지운다. 같은 코드를 두 번 쓸 수 없게 하기 위해서다. */
    Optional<Pairing> take(PairingCode code);
}
