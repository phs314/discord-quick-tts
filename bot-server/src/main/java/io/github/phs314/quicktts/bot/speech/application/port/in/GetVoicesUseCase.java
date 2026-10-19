package io.github.phs314.quicktts.bot.speech.application.port.in;

import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import java.util.List;

/**
 * 사용자가 고를 수 있는 목소리 목록을 본다.
 */
public interface GetVoicesUseCase {

    List<Voice> getVoices();
}
