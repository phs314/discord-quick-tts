package io.github.phs314.quicktts.bot.device.domain;

import io.github.phs314.quicktts.bot.shared.domain.BusinessException;
import io.github.phs314.quicktts.bot.shared.domain.ErrorKind;
import io.github.phs314.quicktts.api.ApiErrorCode;

/**
 * 없는 연결 코드이거나 만료된 연결 코드일 때 던진다.
 */
public class InvalidPairingCodeException extends BusinessException {

    public InvalidPairingCodeException() {
        super(ErrorKind.INVALID_ARGUMENT, ApiErrorCode.INVALID_PAIRING_CODE,
                "연결 코드가 없거나 만료되었습니다. 디스코드에서 /연결 을 다시 입력해 주세요.");
    }
}
