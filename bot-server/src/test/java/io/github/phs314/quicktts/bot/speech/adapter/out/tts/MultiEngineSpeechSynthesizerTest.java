package io.github.phs314.quicktts.bot.speech.adapter.out.tts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.speech.application.exception.SpeechSynthesisException;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.SpokenText;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class MultiEngineSpeechSynthesizerTest {

    private static final VoiceId EDGE = new VoiceId("edge:ko-KR-SunHiNeural");
    private static final SpokenText HELLO = new SpokenText("안녕");

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @Test
    void 엔진들의_목소리를_한_목록으로_내놓고_설정한_기본_목소리를_쓴다() {
        MultiEngineSpeechSynthesizer synthesizer = synthesizer(new FakeEngine(EDGE, false), defaultVoice(EDGE.value()));

        assertThat(synthesizer.voices()).extracting(Voice::id).containsExactly(EDGE, GoogleTranslateEngine.VOICE_ID);
        assertThat(synthesizer.defaultVoice()).isEqualTo(EDGE);
    }

    @Test
    void 설정한_기본_목소리가_없으면_첫_목소리를_쓴다() {
        MultiEngineSpeechSynthesizer synthesizer = synthesizer(new FakeEngine(EDGE, false), defaultVoice("edge:없는-목소리"));

        assertThat(synthesizer.defaultVoice()).isEqualTo(EDGE);
    }

    @Test
    void 고른_목소리의_엔진으로_읽는다() {
        MultiEngineSpeechSynthesizer synthesizer = synthesizer(new FakeEngine(EDGE, false), defaultVoice(EDGE.value()));

        assertThat(text(synthesizer.synthesize(HELLO, EDGE))).isEqualTo(EDGE.value() + ":안녕");
    }

    @Test
    void 고른_엔진이_실패하면_Google_번역_목소리로_대신_읽는다() {
        MultiEngineSpeechSynthesizer synthesizer = synthesizer(new FakeEngine(EDGE, true), defaultVoice(EDGE.value()));

        assertThat(text(synthesizer.synthesize(HELLO, EDGE))).isEqualTo(GoogleTranslateEngine.VOICE_ID.value() + ":안녕");
    }

    @Test
    void 목소리별로_걸린_시간과_결과를_센다() {
        MultiEngineSpeechSynthesizer synthesizer = synthesizer(new FakeEngine(EDGE, false), defaultVoice(EDGE.value()));

        synthesizer.synthesize(HELLO, EDGE);

        assertThat(synthesisCount(EDGE, "success")).isEqualTo(1);
        assertThat(meterRegistry.find(MultiEngineSpeechSynthesizer.FALLBACK_METRIC).counter()).isNull();
    }

    @Test
    void 대신_읽으면_실패와_대신_읽은_횟수를_센다() {
        MultiEngineSpeechSynthesizer synthesizer = synthesizer(new FakeEngine(EDGE, true), defaultVoice(EDGE.value()));

        synthesizer.synthesize(HELLO, EDGE);

        assertThat(synthesisCount(EDGE, "failure")).isEqualTo(1);
        assertThat(synthesisCount(GoogleTranslateEngine.VOICE_ID, "success")).isEqualTo(1);
        assertThat(meterRegistry.get(MultiEngineSpeechSynthesizer.FALLBACK_METRIC)
                .tag("voice", EDGE.value()).counter().count()).isEqualTo(1);
    }

    @Test
    void 없는_목소리로는_읽을_수_없다() {
        MultiEngineSpeechSynthesizer synthesizer = new MultiEngineSpeechSynthesizer(
                List.of(new FakeEngine(EDGE, false)), defaultVoice(EDGE.value()), meterRegistry);

        assertThatThrownBy(() -> synthesizer.synthesize(HELLO, new VoiceId("edge:없는-목소리")))
                .isInstanceOf(SpeechSynthesisException.class);
    }

    private MultiEngineSpeechSynthesizer synthesizer(TtsEngine edge, TtsProperties properties) {
        return new MultiEngineSpeechSynthesizer(
                List.of(edge, new FakeEngine(GoogleTranslateEngine.VOICE_ID, false)), properties, meterRegistry);
    }

    private long synthesisCount(VoiceId voiceId, String outcome) {
        return meterRegistry.get(MultiEngineSpeechSynthesizer.SYNTHESIS_METRIC)
                .tag("voice", voiceId.value()).tag("outcome", outcome).timer().count();
    }

    private static TtsProperties defaultVoice(String voice) {
        return new TtsProperties(voice, "143.0.3650.75");
    }

    private static String text(Speech speech) {
        return new String(speech.audio(), StandardCharsets.UTF_8);
    }

    /** 목소리 하나만 내는 가짜 엔진. 음성 대신 "목소리:문장" 을 돌려준다. */
    private record FakeEngine(VoiceId voiceId, boolean fails) implements TtsEngine {

        @Override
        public List<Voice> voices() {
            return List.of(new Voice(voiceId, voiceId.value()));
        }

        @Override
        public Speech synthesize(String text, VoiceId requested) {
            if (fails) {
                throw new SpeechSynthesisException("실패");
            }
            return new Speech((requested.value() + ":" + text).getBytes(StandardCharsets.UTF_8), "txt");
        }
    }
}
