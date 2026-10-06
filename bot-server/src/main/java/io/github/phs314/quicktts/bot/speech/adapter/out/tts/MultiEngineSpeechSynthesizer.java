package io.github.phs314.quicktts.bot.speech.adapter.out.tts;

import io.github.phs314.quicktts.bot.config.QuickTtsProperties;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesisException;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.QuickChatMessage;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 여러 {@link TtsEngine} 의 목소리를 한 목록으로 내놓고, 고른 목소리의 엔진으로 읽는다.
 * 고른 엔진이 실패하면 Google 번역 목소리로 대신 읽어서 아무 소리도 안 나는 일을 줄인다.
 */
@Component
public class MultiEngineSpeechSynthesizer implements SpeechSynthesizerPort {

    private static final Logger log = LoggerFactory.getLogger(MultiEngineSpeechSynthesizer.class);

    private final List<TtsEngine> engines;
    private final List<Voice> voices;
    private final VoiceId defaultVoice;

    MultiEngineSpeechSynthesizer(List<TtsEngine> engines, QuickTtsProperties properties) {
        this.engines = engines;
        this.voices = engines.stream().flatMap(engine -> engine.voices().stream()).toList();
        VoiceId configured = new VoiceId(properties.tts().defaultVoice());
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
    public Speech synthesize(QuickChatMessage message, VoiceId voice) {
        try {
            return engineOf(voice).synthesize(message.text(), voice);
        } catch (SpeechSynthesisException e) {
            if (voice.equals(GoogleTranslateEngine.VOICE_ID) || !offers(GoogleTranslateEngine.VOICE_ID)) {
                throw e;
            }
            log.warn("{} 목소리로 읽지 못해 Google 번역 목소리로 대신 읽습니다.", voice.value(), e);
            return engineOf(GoogleTranslateEngine.VOICE_ID).synthesize(message.text(), GoogleTranslateEngine.VOICE_ID);
        }
    }

    private TtsEngine engineOf(VoiceId voice) {
        return engines.stream()
                .filter(engine -> engine.offers(voice))
                .findFirst()
                .orElseThrow(() -> new SpeechSynthesisException("이 목소리를 낼 수 있는 엔진이 없습니다: " + voice.value()));
    }

    private boolean offers(VoiceId voice) {
        return engines.stream().anyMatch(engine -> engine.offers(voice));
    }
}
