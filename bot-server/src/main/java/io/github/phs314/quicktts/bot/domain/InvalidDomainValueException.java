package io.github.phs314.quicktts.bot.domain;

/**
 * 도메인 값이 규칙에 맞지 않을 때 던진다.
 */
public class InvalidDomainValueException extends RuntimeException {

    public InvalidDomainValueException(String message) {
        super(message);
    }

    public InvalidDomainValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
