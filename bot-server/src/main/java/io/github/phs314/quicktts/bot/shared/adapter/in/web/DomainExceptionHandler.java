package io.github.phs314.quicktts.bot.shared.adapter.in.web;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 어느 컨텍스트에서든 도메인 값 검증에 실패하면 400 으로 돌려준다.
 */
@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(InvalidDomainValueException.class)
    public ProblemDetail invalidRequest(InvalidDomainValueException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
