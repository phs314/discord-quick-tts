package io.github.phs314.quicktts.bot.settings.adapter.out.synthesis;

import io.github.phs314.quicktts.bot.settings.application.port.out.VoiceChoicesPort;
import io.github.phs314.quicktts.bot.settings.domain.vo.VoiceChoice;
import io.github.phs314.quicktts.bot.settings.domain.vo.VoiceChoices;
import io.github.phs314.quicktts.bot.synthesis.application.port.in.GetVoicesUseCase;
import io.github.phs314.quicktts.bot.synthesis.application.port.in.dto.VoiceCatalogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * synthesis 에 엔진들이 낼 수 있는 목소리를 물어 고를 수 있는 목소리로 바꾼다 (ADR 0040).
 */
@Component
@RequiredArgsConstructor
public class VoiceChoicesSynthesisAdapter implements VoiceChoicesPort {

    private final GetVoicesUseCase getVoicesUseCase;

    @Override
    public VoiceChoices getVoiceChoices() {
        VoiceCatalogDto catalog = getVoicesUseCase.getVoices();
        return new VoiceChoices(
                catalog.voices().stream().map(voice -> new VoiceChoice(voice.id(), voice.label())).toList(),
                catalog.defaultVoiceId());
    }
}
