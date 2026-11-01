package io.github.phs314.quicktts.bot.speech.domain;

import io.github.phs314.quicktts.bot.shared.domain.BusinessException;
import io.github.phs314.quicktts.bot.shared.domain.ErrorKind;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import io.github.phs314.quicktts.api.ApiErrorCode;

/**
 * 고를 수 있는 목소리 목록에 없는 목소리를 골랐을 때 던진다.
 */
public class UnknownVoiceException extends BusinessException {

    public UnknownVoiceException(VoiceId voiceId) {
        super(ErrorKind.INVALID_ARGUMENT, ApiErrorCode.UNKNOWN_VOICE, "없는 목소리입니다: " + voiceId.value());
    }
}
