package io.github.phs314.quicktts.bot.synthesis.application.port.in;

import io.github.phs314.quicktts.bot.synthesis.application.port.in.command.SynthesizeSpeechCommand;
import io.github.phs314.quicktts.bot.synthesis.application.port.in.dto.AudioDto;

/**
 * 문장을 고른 목소리의 음성으로 만든다. quick chat 을 읽어 주는 speech 가 부른다.
 */
public interface SynthesizeSpeechUseCase {

    /**
     * @throws io.github.phs314.quicktts.bot.synthesis.application.exception.SpeechSynthesisException 음성 생성에 실패했을 때
     */
    AudioDto synthesize(SynthesizeSpeechCommand command);
}
