package io.github.phs314.quicktts.bot.speech.application;

import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;

/**
 * 고를 수 있는 목소리 목록에 없는 목소리를 골랐을 때 던진다.
 */
public class UnknownVoiceException extends RuntimeException {

    public UnknownVoiceException(VoiceId voice) {
        super("없는 목소리입니다: " + voice.value());
    }
}
