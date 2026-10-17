package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.ManageVoiceUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoicePreferencePort;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceCatalog;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public Voice getCurrentVoice(DiscordUserId user) {
        return catalog().chosenOrDefault(voicePreferencePort.findVoice(user));
    }

    @Override
    public Voice changeVoice(DiscordUserId user, VoiceId voiceId) {
        Voice voice = catalog().choose(voiceId);
        voicePreferencePort.saveVoice(user, voice.id());
        return voice;
    }

    private VoiceCatalog catalog() {
        return new VoiceCatalog(speechSynthesizerPort.voices(), speechSynthesizerPort.defaultVoice());
    }
}
