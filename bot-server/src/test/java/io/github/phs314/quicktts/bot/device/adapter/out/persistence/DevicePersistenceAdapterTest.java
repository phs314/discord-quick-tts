package io.github.phs314.quicktts.bot.device.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceTokenHash;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.support.TestDatabase;
import io.github.phs314.quicktts.bot.support.TestJpa;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@ParameterizedClass
@EnumSource(TestDatabase.class)
class DevicePersistenceAdapterTest {

    private static final DiscordUserId USER = new DiscordUserId(123456789012345678L);
    private static final DiscordUserId OTHER = new DiscordUserId(987654321098765432L);
    private static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Parameter
    TestDatabase database;

    private AnnotationConfigApplicationContext context;
    private DevicePersistenceAdapter repository;

    @BeforeEach
    void setUp() {
        context = TestJpa.context(database.dataSource(), DevicePersistenceAdapter.class);
        repository = context.getBean(DevicePersistenceAdapter.class);
    }

    @AfterEach
    void tearDown() {
        // Docker 가 없어 MySQL 쪽을 건너뛰면 문맥을 만들기 전에 끝난다.
        if (context != null) {
            context.close();
        }
    }

    @Test
    void 저장한_기기를_토큰_해시로_찾는다() {
        Device device = device(USER, "a", "현수-PC", NOW);

        repository.save(device);

        assertThat(repository.findByTokenHash(device.tokenHash())).get()
                .usingRecursiveComparison().isEqualTo(device);
        assertThat(repository.findByTokenHash(new DeviceTokenHash("f".repeat(64)))).isEmpty();
    }

    @Test
    void 그_사용자의_기기만_등록한_순서대로_돌려준다() {
        Device laptop = device(USER, "b", "노트북", NOW.plusSeconds(10));
        Device desktop = device(USER, "a", "데스크톱", NOW);
        repository.save(laptop);
        repository.save(desktop);
        repository.save(device(OTHER, "c", "남의-PC", NOW));

        assertThat(repository.findByUserId(USER))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(desktop, laptop);
    }

    @Test
    void ID_로_찾고_지운다() {
        Device device = device(USER, "a", "현수-PC", NOW);
        repository.save(device);

        assertThat(repository.findById(device.id())).get().usingRecursiveComparison().isEqualTo(device);
        assertThat(repository.delete(device.id())).isTrue();
        assertThat(repository.delete(device.id())).isFalse();
        assertThat(repository.findById(device.id())).isEmpty();
    }

    @Test
    void 그_사용자의_기기를_모두_지우고_지운_수를_돌려준다() {
        repository.save(device(USER, "a", "데스크톱", NOW));
        repository.save(device(USER, "b", "노트북", NOW));
        repository.save(device(OTHER, "c", "남의-PC", NOW));

        assertThat(repository.deleteAllByUserId(USER)).isEqualTo(2);
        assertThat(repository.findByUserId(OTHER)).hasSize(1);
    }

    private static Device device(DiscordUserId userId, String hashSeed, String name, Instant registeredAt) {
        return new Device(DeviceId.generate(), new DeviceTokenHash(hashSeed.repeat(64)), userId, new DeviceName(name), registeredAt);
    }
}
