package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.port.in.command.ChangeVoiceCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.query.GetCurrentVoiceQuery;
import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.ManageVoiceUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoicePreferencePort;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceCatalog;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManageVoiceService implements ManageVoiceUseCase {

    private final SpeechSynthesizerPort speechSynthesizerPort;
    private final VoicePreferencePort voicePreferencePort;

    @Override
    public List<Voice> getVoices() {
        return speechSynthesizerPort.voices();
    }

    @Override
    public Voice getCurrentVoice(GetCurrentVoiceQuery query) {
        return catalog().chosenOrDefault(voicePreferencePort.findVoice(query.user()));
    }

    @Override
    @Transactional
    public Voice changeVoice(ChangeVoiceCommand command) {
        Voice voice = catalog().choose(command.voiceId());
        voicePreferencePort.saveVoice(command.user(), voice.id());
        return voice;
    }

    private VoiceCatalog catalog() {
        return new VoiceCatalog(speechSynthesizerPort.voices(), speechSynthesizerPort.defaultVoice());
    }
}
