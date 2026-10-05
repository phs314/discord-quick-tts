package io.github.phs314.quicktts.bot.speech.adapter.in.web;

import io.github.phs314.quicktts.bot.speech.application.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * speech 컨텍스트의 예외를 HTTP 응답으로 바꾼다. 상태 코드는 클라이언트가 안내 문구를 고르는 데 쓴다.
 */
@RestControllerAdvice
public class SpeechExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SpeechExceptionHandler.class);

    @ExceptionHandler(SpeakerNotInVoiceChannelException.class)
    public ProblemDetail speakerNotInVoiceChannel(SpeakerNotInVoiceChannelException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(SpeechSynthesisException.class)
    public ProblemDetail speechSynthesisFailed(SpeechSynthesisException e) {
        log.warn("음성 생성에 실패했습니다.", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, e.getMessage());
    }
}
