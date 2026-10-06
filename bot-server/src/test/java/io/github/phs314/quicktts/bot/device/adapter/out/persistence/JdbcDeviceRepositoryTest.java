package io.github.phs314.quicktts.bot.device.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

class JdbcDeviceRepositoryTest {

    private static final DiscordUserId OWNER = new DiscordUserId(123456789012345678L);
    private static final DiscordUserId OTHER = new DiscordUserId(987654321098765432L);
    private static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private DataSource dataSource;
    private JdbcDeviceRepository repository;

    @BeforeEach
    void setUp() {
        dataSource = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        runSchema();
        repository = new JdbcDeviceRepository(JdbcClient.create(dataSource));
    }

    @Test
    void 저장한_기기를_토큰_해시로_찾는다() {
        Device device = device(OWNER, "a", "현수-PC", NOW);

        repository.save(device);

        assertThat(repository.findByTokenHash(device.tokenHash())).get()
                .usingRecursiveComparison().isEqualTo(device);
        assertThat(repository.findByTokenHash("z".repeat(64))).isEmpty();
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
    void 주인이_맞을_때만_지운다() {
        Device device = device(OWNER, "a", "현수-PC", NOW);
        repository.save(device);

        assertThat(repository.deleteByIdAndOwner(device.id(), OTHER)).isFalse();
        assertThat(repository.deleteByIdAndOwner(device.id(), OWNER)).isTrue();
        assertThat(repository.findByOwner(OWNER)).isEmpty();
    }

    @Test
    void 주인의_기기를_모두_지우고_지운_수를_돌려준다() {
        repository.save(device(OWNER, "a", "데스크톱", NOW));
        repository.save(device(OWNER, "b", "노트북", NOW));
        repository.save(device(OTHER, "c", "남의-PC", NOW));

        assertThat(repository.deleteAllByOwner(OWNER)).isEqualTo(2);
        assertThat(repository.findByOwner(OTHER)).hasSize(1);
    }

    @Test
    void 이름과_식별자가_없던_예전_기기도_스키마를_다시_돌리면_목록에_나온다() {
        JdbcClient jdbc = JdbcClient.create(dataSource);
        jdbc.sql("drop table device").update();
        jdbc.sql("""
                create table device (
                    token_hash varchar(64) primary key,
                    discord_user_id bigint not null,
                    registered_at timestamp not null)
                """).update();
        jdbc.sql("insert into device values ('" + "a".repeat(64) + "', :owner, current_timestamp)")
                .param("owner", OWNER.value())
                .update();

        runSchema();

        assertThat(repository.findByOwner(OWNER)).singleElement().satisfies(device -> {
            assertThat(device.id().value()).isNotBlank();
            assertThat(device.name()).isEqualTo(new DeviceName(null));
        });
    }

    private void runSchema() {
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);
    }

    private static Device device(DiscordUserId owner, String hashSeed, String name, Instant registeredAt) {
        return new Device(DeviceId.generate(), hashSeed.repeat(64), owner, new DeviceName(name), registeredAt);
    }
}
