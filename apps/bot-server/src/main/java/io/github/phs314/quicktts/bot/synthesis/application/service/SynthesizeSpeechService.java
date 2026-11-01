package io.github.phs314.quicktts.bot.synthesis.application.service;

import io.github.phs314.quicktts.bot.synthesis.application.port.in.SynthesizeSpeechUseCase;
import io.github.phs314.quicktts.bot.synthesis.application.port.in.command.SynthesizeSpeechCommand;
import io.github.phs314.quicktts.bot.synthesis.application.port.in.dto.AudioDto;
import io.github.phs314.quicktts.bot.synthesis.application.port.out.SpeechSynthesizerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SynthesizeSpeechService implements SynthesizeSpeechUseCase {

    private final SpeechSynthesizerPort speechSynthesizerPort;

    @Override
    public AudioDto synthesize(SynthesizeSpeechCommand command) {
        return AudioDto.from(speechSynthesizerPort.synthesize(command.text(), command.voiceId()));
    }
}
