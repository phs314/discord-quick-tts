package io.github.phs314.quicktts.bot.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.application.InvalidPairingCodeException;
import io.github.phs314.quicktts.bot.application.port.out.DeviceRepository;
import io.github.phs314.quicktts.bot.application.port.out.PairingRepository;
import io.github.phs314.quicktts.bot.domain.DiscordUserId;
import io.github.phs314.quicktts.bot.domain.device.Device;
import io.github.phs314.quicktts.bot.domain.device.DeviceToken;
import io.github.phs314.quicktts.bot.domain.device.Pairing;
import io.github.phs314.quicktts.bot.domain.device.PairingCode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DeviceRegistrationServiceTest {

    private static final DiscordUserId OWNER = new DiscordUserId(42L);
    private static final Instant NOW = Instant.parse("2026-10-05T00:00:00Z");

    private final FakePairingRepository pairings = new FakePairingRepository();
    private final FakeDeviceRepository devices = new FakeDeviceRepository();

    @Test
    void 연결_코드로_등록한_기기_토큰은_코드를_받은_사용자로_인증된다() {
        Pairing pairing = serviceAt(NOW).issue(OWNER);

        DeviceToken token = serviceAt(NOW.plusSeconds(30)).register(pairing.code());

        assertThat(serviceAt(NOW).authenticate(token)).contains(OWNER);
    }

    @Test
    void 연결_코드는_한_번만_쓸_수_있다() {
        Pairing pairing = serviceAt(NOW).issue(OWNER);
        serviceAt(NOW).register(pairing.code());

        assertThatThrownBy(() -> serviceAt(NOW).register(pairing.code()))
                .isInstanceOf(InvalidPairingCodeException.class);
    }

    @Test
    void 만료된_연결_코드로는_등록할_수_없다() {
        Pairing pairing = serviceAt(NOW).issue(OWNER);
        Instant afterExpiry = NOW.plus(Pairing.VALID_FOR).plus(Duration.ofSeconds(1));

        assertThatThrownBy(() -> serviceAt(afterExpiry).register(pairing.code()))
                .isInstanceOf(InvalidPairingCodeException.class);
    }

    @Test
    void 등록되지_않은_토큰은_인증되지_않는다() {
        assertThat(serviceAt(NOW).authenticate(DeviceToken.generate())).isEmpty();
    }

    @Test
    void 서버에는_토큰_원문이_아니라_해시만_저장한다() {
        Pairing pairing = serviceAt(NOW).issue(OWNER);
        DeviceToken token = serviceAt(NOW).register(pairing.code());

        assertThat(devices.byHash).containsOnlyKeys(token.hash());
        assertThat(token.hash()).isNotEqualTo(token.value());
    }

    private DeviceRegistrationService serviceAt(Instant now) {
        return new DeviceRegistrationService(pairings, devices, Clock.fixed(now, ZoneOffset.UTC));
    }

    private static class FakePairingRepository implements PairingRepository {

        private final Map<PairingCode, Pairing> byCode = new HashMap<>();

        @Override
        public void save(Pairing pairing) {
            byCode.put(pairing.code(), pairing);
        }

        @Override
        public Optional<Pairing> take(PairingCode code) {
            return Optional.ofNullable(byCode.remove(code));
        }
    }

    private static class FakeDeviceRepository implements DeviceRepository {

        private final Map<String, Device> byHash = new HashMap<>();

        @Override
        public void save(Device device) {
            byHash.put(device.tokenHash(), device);
        }

        @Override
        public Optional<Device> findByTokenHash(String tokenHash) {
            return Optional.ofNullable(byHash.get(tokenHash));
        }
    }
}
