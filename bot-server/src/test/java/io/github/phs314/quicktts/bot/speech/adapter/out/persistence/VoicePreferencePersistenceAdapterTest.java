package io.github.phs314.quicktts.bot.speech.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
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
class VoicePreferencePersistenceAdapterTest {

    @Parameter
    TestDatabase database;

    private static final DiscordUserId USER = new DiscordUserId(123456789012345678L);

    private AnnotationConfigApplicationContext context;
    private VoicePreferencePersistenceAdapter repository;

    @BeforeEach
    void setUp() {
        context = TestJpa.context(database.dataSource(), VoicePreferencePersistenceAdapter.class);
        repository = context.getBean(VoicePreferencePersistenceAdapter.class);
    }

    @AfterEach
    void tearDown() {
        // Docker 가 없어 MySQL 쪽을 건너뛰면 문맥을 만들기 전에 끝난다.
        if (context != null) {
            context.close();
        }
    }

    @Test
    void 고른_적이_없으면_비어_있다() {
        assertThat(repository.findVoice(USER)).isEmpty();
    }

    @Test
    void 다시_고르면_마지막_목소리로_바뀐다() {
        repository.saveVoice(USER, new VoiceId("edge:ko-KR-SunHiNeural"));
        repository.saveVoice(USER, new VoiceId("edge:ko-KR-InJoonNeural"));

        assertThat(repository.findVoice(USER)).contains(new VoiceId("edge:ko-KR-InJoonNeural"));
    }
}
