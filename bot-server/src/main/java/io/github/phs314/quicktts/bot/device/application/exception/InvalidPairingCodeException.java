package io.github.phs314.quicktts.bot.device.application.exception;

/**
 * 없는 연결 코드이거나 만료된 연결 코드일 때 던진다.
 */
public class InvalidPairingCodeException extends RuntimeException {

    public InvalidPairingCodeException() {
        super("연결 코드가 없거나 만료되었습니다. 디스코드에서 /연결 을 다시 입력해 주세요.");
    }
}
