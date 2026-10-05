package io.github.phs314.quicktts.bot.speech.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.shared.domain.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.VoiceId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

class JdbcVoicePreferenceRepositoryTest {

    private static final DiscordUserId USER = new DiscordUserId(123456789012345678L);

    private JdbcVoicePreferenceRepository repository;

    @BeforeEach
    void setUp() {
        var dataSource = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);
        repository = new JdbcVoicePreferenceRepository(JdbcClient.create(dataSource));
    }

    @Test
    void 고른_적이_없으면_비어_있다() {
        assertThat(repository.find(USER)).isEmpty();
    }

    @Test
    void 다시_고르면_마지막_목소리로_바뀐다() {
        repository.save(USER, new VoiceId("edge:ko-KR-SunHiNeural"));
        repository.save(USER, new VoiceId("edge:ko-KR-InJoonNeural"));

        assertThat(repository.find(USER)).contains(new VoiceId("edge:ko-KR-InJoonNeural"));
    }
}
