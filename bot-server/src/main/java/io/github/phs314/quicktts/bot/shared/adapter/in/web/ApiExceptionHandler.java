package io.github.phs314.quicktts.bot.shared.adapter.in.web;

import io.github.phs314.quicktts.bot.shared.domain.BusinessException;
import io.github.phs314.quicktts.bot.shared.domain.ErrorKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 어느 컨텍스트의 예상한 실패든 그 종류로 HTTP 상태를 정하고 오류 코드를 실어 돌려준다.
 * 예외를 새로 만들어도 여기는 고치지 않는다. 클라이언트는 상태가 아니라 오류 코드로 안내 문구를 고른다.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail businessFailure(BusinessException e) {
        if (e.kind() == ErrorKind.EXTERNAL_FAILURE) {
            log.warn("바깥 서비스가 실패했습니다.", e);
        }
        return ApiProblem.of(statusOf(e.kind()), e.code(), e.getMessage());
    }

    static HttpStatus statusOf(ErrorKind kind) {
        return switch (kind) {
            case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case CONFLICT -> HttpStatus.CONFLICT;
            case IN_USE -> HttpStatus.LOCKED;
            case EXTERNAL_FAILURE -> HttpStatus.BAD_GATEWAY;
        };
    }
}
