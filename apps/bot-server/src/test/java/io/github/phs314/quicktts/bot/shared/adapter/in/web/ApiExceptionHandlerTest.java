package io.github.phs314.quicktts.bot.shared.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.shared.domain.ErrorKind;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiExceptionHandlerTest {

    /** 이미 배포된 클라이언트가 받는 상태 코드라서 종류마다 고정한다 (ADR 0025). */
    @Test
    void 종류마다_정해진_HTTP_상태로_바꾼다() {
        Map<ErrorKind, HttpStatus> expected = Map.of(
                ErrorKind.INVALID_ARGUMENT, HttpStatus.BAD_REQUEST,
                ErrorKind.UNAUTHENTICATED, HttpStatus.UNAUTHORIZED,
                ErrorKind.PERMISSION_DENIED, HttpStatus.FORBIDDEN,
                ErrorKind.FAILED_PRECONDITION, HttpStatus.CONFLICT,
                ErrorKind.UNAVAILABLE, HttpStatus.BAD_GATEWAY);

        assertThat(expected).containsOnlyKeys(ErrorKind.values());
        expected.forEach((kind, status) -> assertThat(ApiExceptionHandler.statusOf(kind)).isEqualTo(status));
    }
}
