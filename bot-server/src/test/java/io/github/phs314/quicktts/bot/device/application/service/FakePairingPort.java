package io.github.phs314.quicktts.bot.device.application.service;

import io.github.phs314.quicktts.bot.device.application.port.out.PairingPort;
import io.github.phs314.quicktts.bot.device.domain.Pairing;
import io.github.phs314.quicktts.bot.device.domain.vo.PairingCode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class FakePairingPort implements PairingPort {

    final Map<PairingCode, Pairing> byCode = new HashMap<>();

    @Override
    public void save(Pairing pairing) {
        byCode.put(pairing.code(), pairing);
    }

    @Override
    public Optional<Pairing> take(PairingCode code) {
        return Optional.ofNullable(byCode.remove(code));
    }
}
