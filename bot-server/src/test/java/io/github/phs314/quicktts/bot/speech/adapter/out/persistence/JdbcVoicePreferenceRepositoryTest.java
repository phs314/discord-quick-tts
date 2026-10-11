package io.github.phs314.quicktts.bot.speech.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import io.github.phs314.quicktts.bot.support.TestDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.jdbc.core.simple.JdbcClient;

@ParameterizedClass
@EnumSource(TestDatabase.class)
class JdbcVoicePreferenceRepositoryTest {

    @Parameter
    TestDatabase database;

    private static final DiscordUserId USER = new DiscordUserId(123456789012345678L);

    private JdbcVoicePreferenceRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JdbcVoicePreferenceRepository(JdbcClient.create(database.dataSource()));
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
