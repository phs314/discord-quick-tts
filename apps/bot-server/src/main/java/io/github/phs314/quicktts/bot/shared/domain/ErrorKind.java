package io.github.phs314.quicktts.bot.shared.domain;

/**
 * 예상한 실패의 종류. 인바운드 어댑터는 예외 클래스가 아니라 이 종류로 응답 모양을 정한다.
 * 예: 웹은 HTTP 상태 코드, 디스코드는 안내 문구. 이름은 HTTP 가 아니라 gRPC 표준 상태 코드를 따른다(Google API 설계 지침과 같음).
 */
public enum ErrorKind {

    /** 요청 값이 규칙에 맞지 않음. 다시 보내도 같다. */
    INVALID_ARGUMENT,

    /** 누가 보냈는지 알 수 없음 (기기 토큰이 없거나 해제됨). */
    UNAUTHENTICATED,

    /** 누가 보냈는지는 알지만 할 수 없음 (봇 주인이 아님, 봇에게 권한이 없음). */
    PERMISSION_DENIED,

    /** 지금 상태에서는 할 수 없음 (문장을 보낸 사람이 음성 채널에 없음, 봇이 다른 음성 채널에서 쓰이고 있음). 상태가 바뀌면 된다. */
    FAILED_PRECONDITION,

    /** 바깥 서비스(TTS 엔진 등)가 실패함. 우리 쪽에서 남길 로그가 있다. */
    UNAVAILABLE
}
