package io.github.phs314.quicktts.bot.application.port.out;

import io.github.phs314.quicktts.bot.domain.QuickChatMessage;
import io.github.phs314.quicktts.bot.domain.Speech;

/**
 * 문장을 음성으로 바꾸는 TTS 엔진 포트. 엔진을 바꾸려면 어댑터를 새로 만들고
 * {@code quicktts.tts.engine} 설정으로 고르면 된다.
 */
public interface SpeechSynthesizer {

    /**
     * @throws SpeechSynthesisException 음성 생성에 실패했을 때
     */
    Speech synthesize(QuickChatMessage message);
}
