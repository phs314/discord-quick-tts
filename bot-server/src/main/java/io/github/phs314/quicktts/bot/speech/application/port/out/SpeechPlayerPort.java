package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;

/**
 * 음성 채널에서 음성을 재생한다. 같은 서버 안에서는 앞 음성이 끝난 뒤 다음 음성을 재생한다.
 */
public interface SpeechPlayerPort {

    void play(VoiceChannel channel, Speech speech);
}
