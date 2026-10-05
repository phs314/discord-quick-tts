package io.github.phs314.quicktts.bot.speech.adapter.out.tts;

import io.github.phs314.quicktts.bot.speech.domain.Speech;
import io.github.phs314.quicktts.bot.speech.domain.Voice;
import io.github.phs314.quicktts.bot.speech.domain.VoiceId;
import java.util.List;

/**
 * TTS 엔진 하나. 엔진을 추가하려면 이 인터페이스를 구현한 빈을 만들면
 * {@link MultiEngineSpeechSynthesizer} 가 그 목소리들을 목록에 넣는다.
 */
interface TtsEngine {

    List<Voice> voices();

    /**
     * @throws io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesisException 음성 생성에 실패했을 때
     */
    Speech synthesize(String text, VoiceId voice);

    default boolean offers(VoiceId voice) {
        return voices().stream().anyMatch(offered -> offered.id().equals(voice));
    }
}
