package io.github.phs314.quicktts.bot.device.adapter.in.web;

import io.github.phs314.quicktts.bot.device.application.exception.InvalidPairingCodeException;
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
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
