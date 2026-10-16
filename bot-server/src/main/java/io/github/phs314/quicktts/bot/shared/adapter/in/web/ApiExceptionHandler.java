package io.github.phs314.quicktts.bot.shared.adapter.in.web;

import io.github.phs314.quicktts.bot.shared.domain.BusinessException;
import io.github.phs314.quicktts.bot.shared.domain.ErrorKind;
import io.github.phs314.quicktts.common.ApiErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 웹 API 의 모든 실패를 오류 코드를 실은 ProblemDetail 로 돌려준다.
 * 어느 컨텍스트의 예상한 실패든 그 종류로 HTTP 상태를 정한다. 예외를 새로 만들어도 여기는 고치지 않는다.
 * 요청 모양이 틀린 실패(스프링이 잡는 것)와 예상하지 못한 실패에도 코드를 실어서, 응답 모양이 하나뿐이게 한다.
 * 클라이언트는 상태가 아니라 오류 코드로 안내 문구를 고른다.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail businessFailure(BusinessException e) {
        if (e.kind() == ErrorKind.EXTERNAL_FAILURE) {
            log.warn("바깥 서비스가 실패했습니다.", e);
        }
        return ApiProblem.of(statusOf(e.kind()), e.code(), e.getMessage());
    }

    /** 버그나 설정 오류처럼 예상하지 못한 실패. 원인은 로그에만 남기고 응답에는 싣지 않는다. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail unexpectedFailure(Exception e) {
        log.error("웹 요청을 처리하지 못했습니다.", e);
        return ApiProblem.of(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.UNEXPECTED_FAILURE,
                "봇 서버가 요청을 처리하지 못했습니다.");
    }

    /** 스프링이 잡는 요청 실패(깨진 JSON, 없는 경로 등)는 스프링이 고른 상태 그대로 두고 오류 코드만 싣는다. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        if (body instanceof ProblemDetail problem) {
            problem.setProperty(ApiErrorCode.PROPERTY, statusCode.is4xxClientError()
                    ? ApiErrorCode.INVALID_REQUEST
                    : ApiErrorCode.UNEXPECTED_FAILURE);
        }
        return super.handleExceptionInternal(e, body, headers, statusCode, request);
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
