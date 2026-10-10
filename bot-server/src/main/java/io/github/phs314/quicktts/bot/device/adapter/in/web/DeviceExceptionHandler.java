package io.github.phs314.quicktts.bot.device.adapter.in.web;

import io.github.phs314.quicktts.bot.device.application.exception.InvalidPairingCodeException;
import io.github.phs314.quicktts.bot.shared.adapter.in.web.ApiProblem;
import io.github.phs314.quicktts.common.ApiErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * device 컨텍스트의 예외를 HTTP 응답으로 바꾼다.
 */
@RestControllerAdvice
public class DeviceExceptionHandler {

    @ExceptionHandler(InvalidPairingCodeException.class)
    public ProblemDetail invalidPairingCode(InvalidPairingCodeException e) {
        return ApiProblem.of(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_PAIRING_CODE, e.getMessage());
    }
}
