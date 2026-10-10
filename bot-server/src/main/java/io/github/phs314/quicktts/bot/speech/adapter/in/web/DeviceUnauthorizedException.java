package io.github.phs314.quicktts.bot.speech.adapter.in.web;

/**
 * 요청에 기기 토큰이 없거나, 해제된 기기의 토큰일 때 던진다. {@link SpeechExceptionHandler} 가 401 로 바꾼다.
 */
class DeviceUnauthorizedException extends RuntimeException {

    DeviceUnauthorizedException() {
        super("기기 토큰이 없거나 연결이 해제된 기기입니다.");
    }
}
