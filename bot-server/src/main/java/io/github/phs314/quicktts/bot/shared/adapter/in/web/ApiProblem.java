package io.github.phs314.quicktts.bot.shared.adapter.in.web;

import io.github.phs314.quicktts.common.ApiErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * 오류 코드({@link ApiErrorCode})를 실은 오류 응답을 만든다. 예외 처리기는 모두 이것으로 응답을 만든다.
 */
public final class ApiProblem {

    private ApiProblem() {
    }

    public static ProblemDetail of(HttpStatus status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty(ApiErrorCode.PROPERTY, code);
        return problem;
    }
}
