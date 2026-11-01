package io.github.phs314.quicktts.bot.speech.application.exception;

import io.github.phs314.quicktts.api.ApiErrorCode;
import io.github.phs314.quicktts.bot.shared.domain.BusinessException;
import io.github.phs314.quicktts.bot.shared.domain.ErrorKind;

/**
 * 받은 음성을 음성 채널에 틀 수 없을 때 던진다. 사용자에게는 음성을 만들지 못한 것과 같아서 같은 오류 코드로 알린다.
 */
public class UnplayableAudioException extends BusinessException {

    public UnplayableAudioException(String message) {
        super(ErrorKind.UNAVAILABLE, ApiErrorCode.SPEECH_SYNTHESIS_FAILED, message);
    }

    public UnplayableAudioException(String message, Throwable cause) {
        super(ErrorKind.UNAVAILABLE, ApiErrorCode.SPEECH_SYNTHESIS_FAILED, message, cause);
    }
}
