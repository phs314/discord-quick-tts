package io.github.phs314.quicktts.bot.device.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.device.application.InvalidPairingCodeException;
import io.github.phs314.quicktts.bot.device.application.port.out.DevicePort;
import io.github.phs314.quicktts.bot.device.application.port.out.PairingPort;
import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.device.domain.Pairing;
import io.github.phs314.quicktts.bot.device.domain.vo.PairingCode;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DeviceRegistrationServiceTest {

    private static final DiscordUserId OWNER = new DiscordUserId(42L);
    private static final Instant NOW = Instant.parse("2026-10-05T00:00:00Z");
    private static final DiscordUserId OTHER = new DiscordUserId(7L);
    private static final DeviceName PC = new DeviceName("현수-PC");

    private final FakePairingPort pairings = new FakePairingPort();
    private final FakeDevicePort devices = new FakeDevicePort();

    @Test
    void 연결_코드로_등록한_기기_토큰은_코드를_받은_사용자로_인증된다() {
        Pairing pairing = serviceAt(NOW).issue(OWNER);

        DeviceToken token = serviceAt(NOW.plusSeconds(30)).register(pairing.code(), PC);

        assertThat(serviceAt(NOW).authenticate(token.value())).contains(OWNER);
    }

    @Test
    void 연결_코드는_한_번만_쓸_수_있다() {
        Pairing pairing = serviceAt(NOW).issue(OWNER);
        serviceAt(NOW).register(pairing.code(), PC);

        assertThatThrownBy(() -> serviceAt(NOW).register(pairing.code(), PC))
                .isInstanceOf(InvalidPairingCodeException.class);
    }

    @Test
    void 만료된_연결_코드로는_등록할_수_없다() {
        Pairing pairing = serviceAt(NOW).issue(OWNER);
        Instant afterExpiry = NOW.plus(Pairing.VALID_FOR).plus(Duration.ofSeconds(1));

        assertThatThrownBy(() -> serviceAt(afterExpiry).register(pairing.code(), PC))
                .isInstanceOf(InvalidPairingCodeException.class);
    }

    @Test
    void 등록되지_않은_토큰은_인증되지_않는다() {
        assertThat(serviceAt(NOW).authenticate(DeviceToken.generate().value())).isEmpty();
    }

    @Test
    void 서버에는_토큰_원문이_아니라_해시만_저장한다() {
        Pairing pairing = serviceAt(NOW).issue(OWNER);
        DeviceToken token = serviceAt(NOW).register(pairing.code(), PC);

        assertThat(devices.byHash).containsOnlyKeys(token.hash());
        assertThat(token.hash()).isNotEqualTo(token.value());
    }

    @Test
    void 내_기기_목록에는_내가_등록한_PC_만_이름과_함께_보인다() {
        registerAs(OWNER, PC);
        registerAs(OTHER, new DeviceName("남의-PC"));

        assertThat(serviceAt(NOW).listDevices(OWNER)).extracting(Device::name).containsExactly(PC);
    }

    @Test
    void 해제한_PC_의_토큰은_더_이상_인증되지_않는다() {
        DeviceToken token = registerAs(OWNER, PC);
        DeviceId id = serviceAt(NOW).listDevices(OWNER).getFirst().id();

        assertThat(serviceAt(NOW).unlink(OWNER, id)).isTrue();
        assertThat(serviceAt(NOW).authenticate(token.value())).isEmpty();
    }

    @Test
    void 다른_사람의_PC_는_해제할_수_없다() {
        DeviceToken othersToken = registerAs(OTHER, PC);
        DeviceId othersId = serviceAt(NOW).listDevices(OTHER).getFirst().id();

        assertThat(serviceAt(NOW).unlink(OWNER, othersId)).isFalse();
        assertThat(serviceAt(NOW).authenticate(othersToken.value())).contains(OTHER);
    }

    @Test
    void 모두_해제하면_내_PC_만_전부_끊긴다() {
        registerAs(OWNER, PC);
        registerAs(OWNER, new DeviceName("노트북"));
        registerAs(OTHER, PC);

        assertThat(serviceAt(NOW).unlinkAll(OWNER)).isEqualTo(2);
        assertThat(serviceAt(NOW).listDevices(OWNER)).isEmpty();
        assertThat(serviceAt(NOW).listDevices(OTHER)).hasSize(1);
    }

    private DeviceToken registerAs(DiscordUserId owner, DeviceName name) {
        Pairing pairing = serviceAt(NOW).issue(owner);
        return serviceAt(NOW).register(pairing.code(), name);
    }

    private DeviceRegistrationService serviceAt(Instant now) {
        return new DeviceRegistrationService(pairings, devices, Clock.fixed(now, ZoneOffset.UTC));
    }

    private static class FakePairingPort implements PairingPort {

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

    private static class FakeDevicePort implements DevicePort {

        private final Map<String, Device> byHash = new HashMap<>();

        @Override
        public void save(Device device) {
            byHash.put(device.tokenHash(), device);
        }

        @Override
        public Optional<Device> findByTokenHash(String tokenHash) {
            return Optional.ofNullable(byHash.get(tokenHash));
        }

        @Override
        public List<Device> findByOwner(DiscordUserId owner) {
            return byHash.values().stream()
                    .filter(device -> device.owner().equals(owner))
                    .sorted(Comparator.comparing(Device::registeredAt))
                    .toList();
        }

        @Override
        public boolean deleteByIdAndOwner(DeviceId id, DiscordUserId owner) {
            return byHash.values().removeIf(device -> device.id().equals(id) && device.owner().equals(owner));
        }

        @Override
        public int deleteAllByOwner(DiscordUserId owner) {
            int before = byHash.size();
            byHash.values().removeIf(device -> device.owner().equals(owner));
            return before - byHash.size();
        }
    }
}
