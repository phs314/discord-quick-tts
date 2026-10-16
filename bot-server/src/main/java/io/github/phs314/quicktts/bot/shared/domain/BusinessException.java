package io.github.phs314.quicktts.bot.shared.domain;

import java.util.Objects;

/**
 * 사용자에게 알려 줄 수 있는, 예상한 실패. 도메인 규칙 위반, 유스케이스 흐름의 실패, 출력 포트가 약속한 바깥 실패가
 * 모두 이것을 상속한다. 코드가 잘못돼 생기는 실패(불변식이 깨짐)는 상속하지 않고 JDK 예외를 쓴다 (ADR 0025).
 */
public abstract class BusinessException extends RuntimeException {

    private final ErrorKind kind;
    private final String code;

    /**
     * @param code 클라이언트와 함께 쓰는 오류 코드 ({@code common} 의 {@code ApiErrorCode})
     */
    protected BusinessException(ErrorKind kind, String code, String message, Throwable cause) {
        super(message, cause);
        this.kind = Objects.requireNonNull(kind);
        this.code = Objects.requireNonNull(code);
    }

    protected BusinessException(ErrorKind kind, String code, String message) {
        this(kind, code, message, null);
    }

    public ErrorKind kind() {
        return kind;
    }

    public String code() {
        return code;
    }
}
