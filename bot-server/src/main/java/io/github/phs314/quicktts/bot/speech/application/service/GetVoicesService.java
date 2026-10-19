package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.port.in.GetVoicesUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetVoicesService implements GetVoicesUseCase {

    private final SpeechSynthesizerPort speechSynthesizerPort;

    @Override
    public List<Voice> getVoices() {
        return speechSynthesizerPort.voices();
    }
}
