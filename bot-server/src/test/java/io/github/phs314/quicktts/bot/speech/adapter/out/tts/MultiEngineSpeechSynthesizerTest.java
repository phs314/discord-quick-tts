package io.github.phs314.quicktts.bot.speech.adapter.out.tts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.config.QuickTtsProperties;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesisException;
import io.github.phs314.quicktts.bot.speech.domain.vo.QuickChatMessage;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class MultiEngineSpeechSynthesizerTest {

    private static final VoiceId EDGE = new VoiceId("edge:ko-KR-SunHiNeural");
    private static final QuickChatMessage HELLO = new QuickChatMessage("안녕");

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
    void 없는_목소리로는_읽을_수_없다() {
        MultiEngineSpeechSynthesizer synthesizer = new MultiEngineSpeechSynthesizer(
                List.of(new FakeEngine(EDGE, false)), defaultVoice(EDGE.value()));

        assertThatThrownBy(() -> synthesizer.synthesize(HELLO, new VoiceId("edge:없는-목소리")))
                .isInstanceOf(SpeechSynthesisException.class);
    }

    private static MultiEngineSpeechSynthesizer synthesizer(TtsEngine edge, QuickTtsProperties properties) {
        return new MultiEngineSpeechSynthesizer(
                List.of(edge, new FakeEngine(GoogleTranslateEngine.VOICE_ID, false)), properties);
    }

    private static QuickTtsProperties defaultVoice(String voice) {
        return new QuickTtsProperties(new QuickTtsProperties.Discord("token"), new QuickTtsProperties.Tts(voice, null));
    }

    private static String text(Speech speech) {
        return new String(speech.audio(), StandardCharsets.UTF_8);
    }

    /** 목소리 하나만 내는 가짜 엔진. 음성 대신 "목소리:문장" 을 돌려준다. */
    private record FakeEngine(VoiceId voice, boolean fails) implements TtsEngine {

        @Override
        public List<Voice> voices() {
            return List.of(new Voice(voice, voice.value()));
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
