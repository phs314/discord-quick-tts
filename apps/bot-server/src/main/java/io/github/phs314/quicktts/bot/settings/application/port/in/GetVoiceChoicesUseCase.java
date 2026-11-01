package io.github.phs314.quicktts.bot.settings.application.port.in;

import io.github.phs314.quicktts.bot.settings.domain.vo.VoiceChoices;

/**
 * 사용자가 고를 수 있는 목소리 목록을 본다. {@code /목소리} 의 선택지가 된다.
 */
public interface GetVoiceChoicesUseCase {

    VoiceChoices getVoiceChoices();
}
