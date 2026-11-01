package io.github.phs314.quicktts.bot.speech.adapter.out.synthesis;

import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import io.github.phs314.quicktts.bot.speech.application.port.out.TextToSpeechPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.Audio;
import io.github.phs314.quicktts.bot.speech.domain.vo.SpokenText;
import io.github.phs314.quicktts.bot.synthesis.application.port.in.SynthesizeSpeechUseCase;
import io.github.phs314.quicktts.bot.synthesis.application.port.in.command.SynthesizeSpeechCommand;
import io.github.phs314.quicktts.bot.synthesis.application.port.in.dto.AudioDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * synthesis 에 다듬은 문장을 넘겨 음성을 받아 온다 (ADR 0040).
 */
@Component
@RequiredArgsConstructor
public class TextToSpeechSynthesisAdapter implements TextToSpeechPort {

    private final SynthesizeSpeechUseCase synthesizeSpeechUseCase;

    @Override
    public Audio synthesize(SpokenText text, VoiceId voiceId) {
        AudioDto audio = synthesizeSpeechUseCase.synthesize(new SynthesizeSpeechCommand(text.text(), voiceId));
        return new Audio(audio.content(), audio.format());
    }
}
