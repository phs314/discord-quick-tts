package io.github.phs314.quicktts.bot.speech.application.port.in;

import io.github.phs314.quicktts.bot.speech.application.port.in.query.GetCurrentVoiceQuery;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;

/**
 * 사용자가 자기 문장을 읽어 줄 목소리를 본다.
 */
public interface GetCurrentVoiceUseCase {

    /** 고른 목소리. 고른 적이 없거나 더 이상 없는 목소리면 기본 목소리. */
    Voice getCurrentVoice(GetCurrentVoiceQuery query);
}
