package io.github.phs314.quicktts.bot.device.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceTokenHash;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.support.TestDatabase;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.jdbc.core.simple.JdbcClient;

@ParameterizedClass
@EnumSource(TestDatabase.class)
class JdbcDeviceRepositoryTest {

    private static final DiscordUserId OWNER = new DiscordUserId(123456789012345678L);
    private static final DiscordUserId OTHER = new DiscordUserId(987654321098765432L);
    private static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Parameter
    TestDatabase database;

    private JdbcDeviceRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JdbcDeviceRepository(JdbcClient.create(database.dataSource()));
    }

    @Test
    void 저장한_기기를_토큰_해시로_찾는다() {
        Device device = device(OWNER, "a", "현수-PC", NOW);

        repository.save(device);

        assertThat(repository.findByTokenHash(device.tokenHash())).get()
                .usingRecursiveComparison().isEqualTo(device);
        assertThat(repository.findByTokenHash(new DeviceTokenHash("f".repeat(64)))).isEmpty();
    }

    @Test
    void 주인의_기기만_등록한_순서대로_돌려준다() {
        Device laptop = device(OWNER, "b", "노트북", NOW.plusSeconds(10));
        Device desktop = device(OWNER, "a", "데스크톱", NOW);
        repository.save(laptop);
        repository.save(desktop);
        repository.save(device(OTHER, "c", "남의-PC", NOW));

        assertThat(repository.findByOwner(OWNER))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(desktop, laptop);
    }

    @Test
    void ID_로_찾고_지운다() {
        Device device = device(OWNER, "a", "현수-PC", NOW);
        repository.save(device);

        assertThat(repository.findById(device.id())).get().usingRecursiveComparison().isEqualTo(device);
        assertThat(repository.delete(device.id())).isTrue();
        assertThat(repository.delete(device.id())).isFalse();
        assertThat(repository.findById(device.id())).isEmpty();
    }

    @Test
    void 주인의_기기를_모두_지우고_지운_수를_돌려준다() {
        repository.save(device(OWNER, "a", "데스크톱", NOW));
        repository.save(device(OWNER, "b", "노트북", NOW));
        repository.save(device(OTHER, "c", "남의-PC", NOW));

        assertThat(repository.deleteAllByOwner(OWNER)).isEqualTo(2);
        assertThat(repository.findByOwner(OTHER)).hasSize(1);
    }

    private static Device device(DiscordUserId owner, String hashSeed, String name, Instant registeredAt) {
        return new Device(DeviceId.generate(), new DeviceTokenHash(hashSeed.repeat(64)), owner, new DeviceName(name), registeredAt);
    }
}
