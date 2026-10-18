package io.github.phs314.quicktts.bot.speech.adapter.out.tts;

import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;

/**
 * TTS 엔진 하나. 엔진을 추가하려면 이 인터페이스를 구현한 빈을 만들면
 * {@link SpeechSynthesizerAdapter} 가 그 목소리들을 목록에 넣는다.
 */
interface TtsEngine {

    String SPEECH_CACHE = "speech";

    List<Voice> voices();

    /**
     * 같은 문장, 같은 목소리면 처음 만든 음성을 캐시에서 돌려준다(키는 두 인자). 실패는 기억하지 않으므로 다음에 다시 시도한다.
     * 캐시된 {@link Speech} 를 여러 곳이 함께 쓰므로 음성 바이트는 읽기만 한다.
     *
     * @throws io.github.phs314.quicktts.bot.speech.application.exception.SpeechSynthesisException 음성 생성에 실패했을 때
     */
    @Cacheable(SPEECH_CACHE)
    Speech synthesize(String text, VoiceId voiceId);

    default boolean offers(VoiceId voiceId) {
        return voices().stream().anyMatch(offered -> offered.id().equals(voiceId));
    }
}
