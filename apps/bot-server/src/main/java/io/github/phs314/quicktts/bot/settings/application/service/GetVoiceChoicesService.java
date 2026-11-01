package io.github.phs314.quicktts.bot.settings.application.service;

import io.github.phs314.quicktts.bot.settings.application.port.in.GetVoiceChoicesUseCase;
import io.github.phs314.quicktts.bot.settings.application.port.out.VoiceChoicesPort;
import io.github.phs314.quicktts.bot.settings.domain.vo.VoiceChoices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetVoiceChoicesService implements GetVoiceChoicesUseCase {

    private final VoiceChoicesPort voiceChoicesPort;

    @Override
    public VoiceChoices getVoiceChoices() {
        return voiceChoicesPort.getVoiceChoices();
    }
}
