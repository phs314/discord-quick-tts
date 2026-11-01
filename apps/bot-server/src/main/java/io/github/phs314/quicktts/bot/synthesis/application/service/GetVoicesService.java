package io.github.phs314.quicktts.bot.synthesis.application.service;

import io.github.phs314.quicktts.bot.synthesis.application.port.in.GetVoicesUseCase;
import io.github.phs314.quicktts.bot.synthesis.application.port.in.dto.VoiceCatalogDto;
import io.github.phs314.quicktts.bot.synthesis.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.synthesis.domain.vo.VoiceCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetVoicesService implements GetVoicesUseCase {

    private final SpeechSynthesizerPort speechSynthesizerPort;

    @Override
    public VoiceCatalogDto getVoices() {
        return VoiceCatalogDto.from(new VoiceCatalog(speechSynthesizerPort.voices(), speechSynthesizerPort.defaultVoice()));
    }
}
