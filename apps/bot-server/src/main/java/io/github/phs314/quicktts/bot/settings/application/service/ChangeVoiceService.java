package io.github.phs314.quicktts.bot.settings.application.service;

import io.github.phs314.quicktts.bot.settings.application.port.in.ChangeVoiceUseCase;
import io.github.phs314.quicktts.bot.settings.application.port.in.command.ChangeVoiceCommand;
import io.github.phs314.quicktts.bot.settings.application.port.out.UserSettingsPort;
import io.github.phs314.quicktts.bot.settings.application.port.out.VoiceChoicesPort;
import io.github.phs314.quicktts.bot.settings.domain.UserSettings;
import io.github.phs314.quicktts.bot.settings.domain.vo.VoiceChoice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeVoiceService implements ChangeVoiceUseCase {

    private final VoiceChoicesPort voiceChoicesPort;
    private final UserSettingsPort userSettingsPort;

    @Override
    @Transactional
    public VoiceChoice changeVoice(ChangeVoiceCommand command) {
        VoiceChoice voice = voiceChoicesPort.getVoiceChoices().choose(command.voiceId());
        UserSettings settings = userSettingsPort.find(command.user())
                .orElseGet(() -> UserSettings.defaults(command.user()));
        settings.changeVoice(voice);
        userSettingsPort.save(settings);
        return voice;
    }
}
