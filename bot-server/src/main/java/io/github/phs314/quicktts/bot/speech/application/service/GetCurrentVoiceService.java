package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.port.in.GetCurrentVoiceUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.query.GetCurrentVoiceQuery;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoicePreferencePort;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCurrentVoiceService implements GetCurrentVoiceUseCase {

    private final SpeechSynthesizerPort speechSynthesizerPort;
    private final VoicePreferencePort voicePreferencePort;

    @Override
    public Voice getCurrentVoice(GetCurrentVoiceQuery query) {
        VoiceCatalog catalog = new VoiceCatalog(speechSynthesizerPort.voices(), speechSynthesizerPort.defaultVoice());
        return catalog.chosenOrDefault(voicePreferencePort.findVoice(query.user()));
    }
}
