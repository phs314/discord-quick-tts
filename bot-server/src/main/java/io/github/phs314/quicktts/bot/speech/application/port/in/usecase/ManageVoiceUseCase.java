package io.github.phs314.quicktts.bot.speech.application.port.in.usecase;

import io.github.phs314.quicktts.bot.speech.application.port.in.command.ChangeVoiceCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.query.GetCurrentVoiceQuery;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import java.util.List;

/**
 * 사용자가 자기 문장을 읽어 줄 목소리를 보고 바꾼다.
 */
public interface ManageVoiceUseCase {

    List<Voice> getVoices();

    /** 고른 목소리. 고른 적이 없거나 더 이상 없는 목소리면 기본 목소리. */
    Voice getCurrentVoice(GetCurrentVoiceQuery query);

    /**
     * @throws io.github.phs314.quicktts.bot.speech.domain.UnknownVoiceException 없는 목소리일 때
     */
    Voice changeVoice(ChangeVoiceCommand command);
}
