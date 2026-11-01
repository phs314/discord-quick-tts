package io.github.phs314.quicktts.bot.speech.adapter.out.tts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.speech.application.exception.SpeechSynthesisException;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 엔진 인터페이스에 붙인 캐시가 실제 빈에서 동작하는지 본다. 스프링 부트처럼 클래스 프록시로 감싼다.
 */
class TtsEngineCacheTest {

    private static final VoiceId SUNHI = new VoiceId("edge:ko-KR-SunHiNeural");
    private static final VoiceId INJOON = new VoiceId("edge:ko-KR-InJoonNeural");

    @Test
    void 같은_문장_같은_목소리는_한_번만_만든다() {
        try (var context = new AnnotationConfigApplicationContext(CacheTestConfig.class)) {
            TtsEngine engine = context.getBean(TtsEngine.class);
            CountingEngine counting = context.getBean(CountingEngine.class);

            Speech first = engine.synthesize("ㅋㅋㅋ", SUNHI);
            Speech second = engine.synthesize("ㅋㅋㅋ", SUNHI);
            engine.synthesize("ㅋㅋㅋ", INJOON);
            engine.synthesize("ㄱㄱ", SUNHI);

            assertThat(second).isSameAs(first);
            assertThat(counting.calls()).isEqualTo(3);
        }
    }

    @Test
    void 실패는_기억하지_않고_다음에_다시_만든다() {
        try (var context = new AnnotationConfigApplicationContext(CacheTestConfig.class)) {
            TtsEngine engine = context.getBean(TtsEngine.class);
            CountingEngine counting = context.getBean(CountingEngine.class);

            assertThatThrownBy(() -> engine.synthesize(CountingEngine.FAILING_TEXT, SUNHI))
                    .isInstanceOf(SpeechSynthesisException.class);
            assertThatThrownBy(() -> engine.synthesize(CountingEngine.FAILING_TEXT, SUNHI))
                    .isInstanceOf(SpeechSynthesisException.class);

            assertThat(counting.calls()).isEqualTo(2);
        }
    }

    @Configuration
    @EnableCaching(proxyTargetClass = true)
    static class CacheTestConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(TtsEngine.SPEECH_CACHE);
        }

        @Bean
        CountingEngine countingEngine() {
            return new CountingEngine();
        }
    }

    /** 몇 번 불렸는지 세는 엔진. {@link #FAILING_TEXT} 는 늘 실패한다. */
    static class CountingEngine implements TtsEngine {

        static final String FAILING_TEXT = "실패";

        private final AtomicInteger calls = new AtomicInteger();

        @Override
        public List<Voice> voices() {
            return List.of(new Voice(SUNHI, "선희"), new Voice(INJOON, "인준"));
        }

        @Override
        public Speech synthesize(String text, VoiceId voiceId) {
            calls.incrementAndGet();
            if (FAILING_TEXT.equals(text)) {
                throw new SpeechSynthesisException("실패");
            }
            return new Speech(new byte[] {1, 2, 3}, "mp3");
        }

        int calls() {
            return calls.get();
        }
    }
}
