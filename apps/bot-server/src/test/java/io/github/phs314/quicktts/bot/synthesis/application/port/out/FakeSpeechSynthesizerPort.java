package io.github.phs314.quicktts.bot.synthesis.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import io.github.phs314.quicktts.bot.synthesis.application.exception.SpeechSynthesisException;
import io.github.phs314.quicktts.bot.synthesis.domain.vo.Audio;
import io.github.phs314.quicktts.bot.synthesis.domain.vo.Voice;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * TTS 엔진 대신 쓰는 대역. 바깥 서비스를 부르지 않고 "목소리:문장" 글을 음성 바이트로 돌려준다. 기본 목소리는 선희다.
 */
public class FakeSpeechSynthesizerPort implements SpeechSynthesizerPort {

    public static final Voice SUNHI = new Voice(new VoiceId("fake:sunhi"), "선희");
    public static final Voice INJOON = new Voice(new VoiceId("fake:injoon"), "인준");

    private volatile boolean failing;

    /** 켜 두면 엔진이 고장 난 것처럼 음성을 만들지 못한다. */
    public void failing(boolean failing) {
        this.failing = failing;
    }

    public void reset() {
        failing = false;
    }

    @Override
    public List<Voice> voices() {
        return List.of(SUNHI, INJOON);
    }

    @Override
    public VoiceId defaultVoice() {
        return SUNHI.id();
    }

    @Override
    public Audio synthesize(String text, VoiceId voiceId) {
        if (failing) {
            throw new SpeechSynthesisException("테스트용 TTS 엔진이 고장 났습니다.");
        }
        return new Audio((voiceId.value() + ":" + text).getBytes(StandardCharsets.UTF_8), "mp3");
    }
}
