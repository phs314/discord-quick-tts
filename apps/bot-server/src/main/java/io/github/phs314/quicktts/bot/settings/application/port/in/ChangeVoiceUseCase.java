package io.github.phs314.quicktts.bot.settings.application.port.in;

import io.github.phs314.quicktts.bot.settings.application.port.in.command.ChangeVoiceCommand;
import io.github.phs314.quicktts.bot.settings.domain.vo.VoiceChoice;

/**
 * 사용자가 자기 문장을 읽어 줄 목소리를 바꾼다.
 */
public interface ChangeVoiceUseCase {

    /**
     * @return 바꾼 목소리
     * @throws io.github.phs314.quicktts.bot.settings.domain.UnknownVoiceException 없는 목소리일 때
     */
    VoiceChoice changeVoice(ChangeVoiceCommand command);
}
