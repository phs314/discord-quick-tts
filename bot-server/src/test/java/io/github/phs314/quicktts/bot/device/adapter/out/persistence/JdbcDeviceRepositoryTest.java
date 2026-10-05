package io.github.phs314.quicktts.bot.device.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.shared.domain.DiscordUserId;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

class JdbcDeviceRepositoryTest {

    private JdbcDeviceRepository repository;

    @BeforeEach
    void setUp() {
        var dataSource = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);
        repository = new JdbcDeviceRepository(JdbcClient.create(dataSource));
    }

    @Test
    void 저장한_기기를_토큰_해시로_찾는다() {
        Device device = new Device("a".repeat(64), new DiscordUserId(123456789012345678L),
                Instant.now().truncatedTo(ChronoUnit.MILLIS));

        repository.save(device);

        assertThat(repository.findByTokenHash(device.tokenHash())).contains(device);
        assertThat(repository.findByTokenHash("b".repeat(64))).isEmpty();
    }
}
