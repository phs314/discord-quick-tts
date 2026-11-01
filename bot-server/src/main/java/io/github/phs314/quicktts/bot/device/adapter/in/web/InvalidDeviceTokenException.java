package io.github.phs314.quicktts.bot.device.adapter.in.web;

import io.github.phs314.quicktts.bot.shared.domain.BusinessException;
import io.github.phs314.quicktts.bot.shared.domain.ErrorKind;
import io.github.phs314.quicktts.common.ApiErrorCode;

/**
 * 기기 토큰이 필요한 요청에 토큰이 없거나 해제된 기기의 토큰일 때 쓴다. 웹 공통 예외 처리가 401 로 바꾼다.
 */
class InvalidDeviceTokenException extends BusinessException {

    InvalidDeviceTokenException() {
        super(ErrorKind.UNAUTHENTICATED, ApiErrorCode.INVALID_DEVICE_TOKEN, "기기 토큰이 없거나 연결이 해제된 기기입니다.");
    }
}
