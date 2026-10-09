package io.github.phs314.quicktts.bot.speech.application.exception;

/**
 * 문장을 음성으로 만들지 못했을 때 던진다. TTS 어댑터가 던지고 유스케이스를 거쳐 웹 어댑터까지 올라간다.
 */
public class SpeechSynthesisException extends RuntimeException {

    public SpeechSynthesisException(String message) {
        super(message);
    }

    public SpeechSynthesisException(String message, Throwable cause) {
        super(message, cause);
    }
}
