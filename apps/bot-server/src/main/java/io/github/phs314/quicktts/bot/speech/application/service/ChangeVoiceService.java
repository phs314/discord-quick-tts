package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.port.in.ChangeVoiceUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.ChangeVoiceCommand;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoicePreferencePort;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeVoiceService implements ChangeVoiceUseCase {

    private final SpeechSynthesizerPort speechSynthesizerPort;
    private final VoicePreferencePort voicePreferencePort;

    @Override
    @Transactional
    public Voice changeVoice(ChangeVoiceCommand command) {
        VoiceCatalog catalog = new VoiceCatalog(speechSynthesizerPort.voices(), speechSynthesizerPort.defaultVoice());
        Voice voice = catalog.choose(command.voiceId());
        voicePreferencePort.saveVoice(command.user(), voice.id());
        return voice;
    }
}
