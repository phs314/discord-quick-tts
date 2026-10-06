package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.speech.domain.vo.QuickChatMessage;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.util.List;

/**
 * 문장을 음성으로 바꾸는 TTS 엔진 포트. 여러 엔진의 목소리를 함께 내놓는다.
 */
public interface SpeechSynthesizerPort {

    /** 고를 수 있는 목소리 목록. 디스코드 선택지에 그대로 쓰므로 25개를 넘지 않는다. */
    List<Voice> voices();

    /** 목소리를 고르지 않은 사용자에게 쓰는 목소리. {@link #voices()} 안에 있다. */
    VoiceId defaultVoice();

    /**
     * @throws SpeechSynthesisException 음성 생성에 실패했을 때
     */
    Speech synthesize(QuickChatMessage message, VoiceId voice);
}
