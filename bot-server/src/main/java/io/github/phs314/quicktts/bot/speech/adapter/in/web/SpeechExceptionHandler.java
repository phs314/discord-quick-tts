package io.github.phs314.quicktts.bot.speech.adapter.in.web;

import io.github.phs314.quicktts.bot.speech.application.exception.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.exception.SpeechSynthesisException;
import io.github.phs314.quicktts.bot.speech.application.exception.VoiceChannelInUseException;
import io.github.phs314.quicktts.bot.shared.adapter.in.web.ApiProblem;
import io.github.phs314.quicktts.common.ApiErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * speech 컨텍스트의 예외를 HTTP 응답으로 바꾼다. 클라이언트는 응답의 오류 코드로 안내 문구를 고른다.
 */
@RestControllerAdvice
public class SpeechExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SpeechExceptionHandler.class);

    @ExceptionHandler(DeviceUnauthorizedException.class)
    public ProblemDetail deviceUnauthorized(DeviceUnauthorizedException e) {
        return ApiProblem.of(HttpStatus.UNAUTHORIZED, ApiErrorCode.DEVICE_UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(SpeakerNotInVoiceChannelException.class)
    public ProblemDetail speakerNotInVoiceChannel(SpeakerNotInVoiceChannelException e) {
        return ApiProblem.of(HttpStatus.CONFLICT, ApiErrorCode.SPEAKER_NOT_IN_VOICE_CHANNEL, e.getMessage());
    }

    @ExceptionHandler(VoiceChannelInUseException.class)
    public ProblemDetail voiceChannelInUse(VoiceChannelInUseException e) {
        return ApiProblem.of(HttpStatus.LOCKED, ApiErrorCode.VOICE_CHANNEL_IN_USE, e.getMessage());
    }

    @ExceptionHandler(SpeechSynthesisException.class)
    public ProblemDetail speechSynthesisFailed(SpeechSynthesisException e) {
        log.warn("음성 생성에 실패했습니다.", e);
        return ApiProblem.of(HttpStatus.BAD_GATEWAY, ApiErrorCode.SPEECH_SYNTHESIS_FAILED, e.getMessage());
    }
}
