package io.github.phs314.quicktts.bot.shared.domain;

import io.github.phs314.quicktts.api.ApiErrorCode;

/**
 * 도메인 값이 규칙에 맞지 않을 때 던진다.
 */
public class InvalidDomainValueException extends BusinessException {

    public InvalidDomainValueException(String message) {
        super(ErrorKind.INVALID_ARGUMENT, ApiErrorCode.INVALID_VALUE, message);
    }

    public InvalidDomainValueException(String message, Throwable cause) {
        super(ErrorKind.INVALID_ARGUMENT, ApiErrorCode.INVALID_VALUE, message, cause);
    }
}
