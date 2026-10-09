package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.exception.UnknownVoiceException;
import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.ManageVoiceUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoicePreferencePort;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManageVoiceService implements ManageVoiceUseCase {

    private final SpeechSynthesizerPort speechSynthesizer;
    private final VoicePreferencePort voicePreferences;

    @Override
    public List<Voice> voices() {
        return speechSynthesizer.voices();
    }

    @Override
    public Voice currentVoice(DiscordUserId user) {
        return voicePreferences.find(user)
                .flatMap(this::findVoice)
                .or(() -> findVoice(speechSynthesizer.defaultVoice()))
                .orElseThrow(() -> new IllegalStateException("기본 목소리가 목소리 목록에 없습니다."));
    }

    @Override
    public Voice changeVoice(DiscordUserId user, VoiceId voiceId) {
        Voice voice = findVoice(voiceId).orElseThrow(() -> new UnknownVoiceException(voiceId));
        voicePreferences.save(user, voice.id());
        return voice;
    }

    private Optional<Voice> findVoice(VoiceId id) {
        return speechSynthesizer.voices().stream().filter(voice -> voice.id().equals(id)).findFirst();
    }
}
