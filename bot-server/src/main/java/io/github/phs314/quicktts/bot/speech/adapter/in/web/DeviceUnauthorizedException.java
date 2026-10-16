package io.github.phs314.quicktts.bot.speech.adapter.in.web;

import io.github.phs314.quicktts.bot.shared.domain.BusinessException;
import io.github.phs314.quicktts.bot.shared.domain.ErrorKind;
import io.github.phs314.quicktts.common.ApiErrorCode;

/**
 * 요청에 기기 토큰이 없거나, 해제된 기기의 토큰일 때 던진다. 웹 공통 예외 처리가 401 로 바꾼다.
 */
class DeviceUnauthorizedException extends BusinessException {

    DeviceUnauthorizedException() {
        super(ErrorKind.UNAUTHENTICATED, ApiErrorCode.DEVICE_UNAUTHORIZED, "기기 토큰이 없거나 연결이 해제된 기기입니다.");
    }
}
