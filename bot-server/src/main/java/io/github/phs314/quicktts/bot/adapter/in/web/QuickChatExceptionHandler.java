package io.github.phs314.quicktts.bot.adapter.in.web;

import io.github.phs314.quicktts.bot.application.InvalidPairingCodeException;
import io.github.phs314.quicktts.bot.application.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.application.port.out.SpeechSynthesisException;
import io.github.phs314.quicktts.bot.domain.InvalidDomainValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 도메인/애플리케이션 예외를 HTTP 응답으로 바꾼다. 상태 코드는 클라이언트가 안내 문구를 고르는 데 쓴다.
 */
@RestControllerAdvice
public class QuickChatExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(QuickChatExceptionHandler.class);

    @ExceptionHandler(InvalidDomainValueException.class)
    public ProblemDetail invalidRequest(InvalidDomainValueException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InvalidPairingCodeException.class)
    public ProblemDetail invalidPairingCode(InvalidPairingCodeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

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
