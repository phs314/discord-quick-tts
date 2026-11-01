package io.github.phs314.quicktts.bot.settings.application.port.out;

import io.github.phs314.quicktts.bot.settings.domain.vo.VoiceChoices;

/**
 * 지금 고를 수 있는 목소리들. 목소리는 엔진을 감싼 synthesis 가 안다.
 */
public interface VoiceChoicesPort {

    VoiceChoices getVoiceChoices();
}
