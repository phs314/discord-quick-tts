package io.github.phs314.quicktts.bot.device.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.device.application.InvalidPairingCodeException;
import io.github.phs314.quicktts.bot.device.domain.Pairing;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RegisterDeviceServiceTest {

    private static final DiscordUserId OWNER = new DiscordUserId(42L);
    private static final Instant NOW = Instant.parse("2026-10-05T00:00:00Z");
    private static final DeviceName PC = new DeviceName("현수-PC");

    private final FakePairingPort pairings = new FakePairingPort();
    private final FakeDevicePort devices = new FakeDevicePort();

    @Test
    void 연결_코드로_등록한_기기는_코드를_받은_사용자의_것이다() {
        Pairing pairing = issuedPairing();

        DeviceToken token = serviceAt(NOW.plusSeconds(30)).register(pairing.code(), PC);

        assertThat(devices.findByTokenHash(token.hash()))
                .hasValueSatisfying(device -> {
                    assertThat(device.owner()).isEqualTo(OWNER);
                    assertThat(device.name()).isEqualTo(PC);
                });
    }

    @Test
    void 연결_코드는_한_번만_쓸_수_있다() {
        Pairing pairing = issuedPairing();
        serviceAt(NOW).register(pairing.code(), PC);

        assertThatThrownBy(() -> serviceAt(NOW).register(pairing.code(), PC))
                .isInstanceOf(InvalidPairingCodeException.class);
    }

    @Test
    void 만료된_연결_코드로는_등록할_수_없다() {
        Pairing pairing = issuedPairing();
        Instant afterExpiry = NOW.plus(Pairing.VALID_FOR).plus(Duration.ofSeconds(1));

        assertThatThrownBy(() -> serviceAt(afterExpiry).register(pairing.code(), PC))
                .isInstanceOf(InvalidPairingCodeException.class);
        assertThat(devices.byHash).isEmpty();
    }

    @Test
    void 서버에는_토큰_원문이_아니라_해시만_저장한다() {
        DeviceToken token = serviceAt(NOW).register(issuedPairing().code(), PC);

        assertThat(devices.byHash).containsOnlyKeys(token.hash());
        assertThat(devices.byHash.values().stream().map(device -> device.tokenHash().value()))
                .doesNotContain(token.value());
    }

    private Pairing issuedPairing() {
        Pairing pairing = Pairing.issue(OWNER, NOW);
        pairings.save(pairing);
        return pairing;
    }

    private RegisterDeviceService serviceAt(Instant now) {
        return new RegisterDeviceService(pairings, devices, Clock.fixed(now, ZoneOffset.UTC));
    }
}
