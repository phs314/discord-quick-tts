package io.github.phs314.quicktts.bot.speech.adapter.out.tts;

import io.github.phs314.quicktts.bot.speech.application.exception.SpeechSynthesisException;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.SpokenText;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 여러 {@link TtsEngine} 의 목소리를 한 목록으로 내놓고, 고른 목소리의 엔진으로 읽는다.
 * 고른 엔진이 실패하면 Google 번역 목소리로 대신 읽어서 아무 소리도 안 나는 일을 줄인다.
 * 목소리별로 걸린 시간과 성공·실패를 {@value #SYNTHESIS_METRIC}, 대신 읽은 횟수를 {@value #FALLBACK_METRIC} 지표로 센다.
 * 비공식 엔진이 막히면 이 두 숫자가 먼저 바뀐다.
 */
@Component
public class SpeechSynthesizerAdapter implements SpeechSynthesizerPort {

    static final String SYNTHESIS_METRIC = "quicktts.tts.synthesis";
    static final String FALLBACK_METRIC = "quicktts.tts.fallback";

    private static final Logger log = LoggerFactory.getLogger(SpeechSynthesizerAdapter.class);

    private final List<TtsEngine> engines;
    private final List<Voice> voices;
    private final VoiceId defaultVoice;
    private final MeterRegistry meterRegistry;

    SpeechSynthesizerAdapter(List<TtsEngine> engines, TtsProperties properties, MeterRegistry meterRegistry) {
        this.engines = engines;
        this.meterRegistry = meterRegistry;
        this.voices = engines.stream().flatMap(engine -> engine.voices().stream()).toList();
        VoiceId configured = new VoiceId(properties.defaultVoice());
        this.defaultVoice = voices.stream().anyMatch(voice -> voice.id().equals(configured))
                ? configured
                : voices.getFirst().id();
    }

    @Override
    public List<Voice> voices() {
        return voices;
    }

    @Override
    public VoiceId defaultVoice() {
        return defaultVoice;
    }

    @Override
    public Speech synthesize(SpokenText text, VoiceId voiceId) {
        try {
            TtsEngine engine = engineOf(voiceId);
            return timed(voiceId, () -> engine.synthesize(text.text(), voiceId));
        } catch (SpeechSynthesisException e) {
            if (voiceId.equals(GoogleTranslateEngine.VOICE_ID) || !offers(GoogleTranslateEngine.VOICE_ID)) {
                throw e;
            }
            log.warn("{} 목소리로 읽지 못해 Google 번역 목소리로 대신 읽습니다.", voiceId.value(), e);
            meterRegistry.counter(FALLBACK_METRIC, "voice", voiceId.value()).increment();
            TtsEngine google = engineOf(GoogleTranslateEngine.VOICE_ID);
            return timed(GoogleTranslateEngine.VOICE_ID,
                    () -> google.synthesize(text.text(), GoogleTranslateEngine.VOICE_ID));
        }
    }

    /** 엔진이 음성을 내놓는 데 걸린 시간을 목소리와 결과(success, failure)별로 잰다. 캐시에서 꺼낸 것도 센다. */
    private Speech timed(VoiceId voiceId, Supplier<Speech> synthesis) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "failure";
        try {
            Speech speech = synthesis.get();
            outcome = "success";
            return speech;
        } finally {
            sample.stop(meterRegistry.timer(SYNTHESIS_METRIC, "voice", voiceId.value(), "outcome", outcome));
        }
    }

    private TtsEngine engineOf(VoiceId voiceId) {
        return engines.stream()
                .filter(engine -> engine.offers(voiceId))
                .findFirst()
                .orElseThrow(() -> new SpeechSynthesisException("이 목소리를 낼 수 있는 엔진이 없습니다: " + voiceId.value()));
    }

    private boolean offers(VoiceId voiceId) {
        return engines.stream().anyMatch(engine -> engine.offers(voiceId));
    }
}
