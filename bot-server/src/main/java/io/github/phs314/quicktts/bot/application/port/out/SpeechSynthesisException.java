package io.github.phs314.quicktts.bot.application.port.out;

public class SpeechSynthesisException extends RuntimeException {

    public SpeechSynthesisException(String message) {
        super(message);
    }

    public SpeechSynthesisException(String message, Throwable cause) {
        super(message, cause);
    }
}
