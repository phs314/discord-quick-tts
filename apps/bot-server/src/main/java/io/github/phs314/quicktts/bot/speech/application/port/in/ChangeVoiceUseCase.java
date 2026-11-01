package io.github.phs314.quicktts.bot.speech.application.port.in;

import io.github.phs314.quicktts.bot.speech.application.port.in.command.ChangeVoiceCommand;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;

/**
 * 사용자가 자기 문장을 읽어 줄 목소리를 바꾼다.
 */
public interface ChangeVoiceUseCase {

    /**
     * @throws io.github.phs314.quicktts.bot.speech.domain.UnknownVoiceException 없는 목소리일 때
     */
    Voice changeVoice(ChangeVoiceCommand command);
}
