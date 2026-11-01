package io.github.phs314.quicktts.bot.settings.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.settings.domain.UserSettings;
import io.github.phs314.quicktts.bot.settings.domain.vo.VoiceChoice;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import io.github.phs314.quicktts.bot.support.TestDatabase;
import io.github.phs314.quicktts.bot.support.TestJpa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@ParameterizedClass
@EnumSource(TestDatabase.class)
class UserSettingsPersistenceAdapterTest {

    @Parameter
    TestDatabase database;

    private static final DiscordUserId USER = new DiscordUserId(123456789012345678L);

    private AnnotationConfigApplicationContext context;
    private UserSettingsPersistenceAdapter repository;

    @BeforeEach
    void setUp() {
        context = TestJpa.context(database.dataSource(), UserSettingsPersistenceAdapter.class);
        repository = context.getBean(UserSettingsPersistenceAdapter.class);
    }

    @AfterEach
    void tearDown() {
        // Docker 가 없어 MySQL 쪽을 건너뛰면 문맥을 만들기 전에 끝난다.
        if (context != null) {
            context.close();
        }
    }

    @Test
    void 저장한_적이_없으면_비어_있다() {
        assertThat(repository.find(USER)).isEmpty();
    }

    @Test
    void 저장하면_고른_목소리를_되살리고_다시_저장하면_바뀐다() {
        repository.save(new UserSettings(USER, new VoiceId("edge:ko-KR-SunHiNeural")));
        UserSettings saved = repository.find(USER).orElseThrow();
        saved.changeVoice(new VoiceChoice(new VoiceId("edge:ko-KR-InJoonNeural"), "인준"));
        repository.save(saved);

        assertThat(repository.find(USER).flatMap(UserSettings::voiceId)).contains(new VoiceId("edge:ko-KR-InJoonNeural"));
    }
}
