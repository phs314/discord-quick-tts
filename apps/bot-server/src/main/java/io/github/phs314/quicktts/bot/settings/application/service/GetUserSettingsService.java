package io.github.phs314.quicktts.bot.settings.application.service;

import io.github.phs314.quicktts.bot.settings.application.port.in.GetUserSettingsUseCase;
import io.github.phs314.quicktts.bot.settings.application.port.in.dto.UserSettingsDto;
import io.github.phs314.quicktts.bot.settings.application.port.in.query.GetUserSettingsQuery;
import io.github.phs314.quicktts.bot.settings.application.port.out.UserSettingsPort;
import io.github.phs314.quicktts.bot.settings.application.port.out.VoiceChoicesPort;
import io.github.phs314.quicktts.bot.settings.domain.UserSettings;
import io.github.phs314.quicktts.bot.settings.domain.vo.VoiceChoice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserSettingsService implements GetUserSettingsUseCase {

    private final VoiceChoicesPort voiceChoicesPort;
    private final UserSettingsPort userSettingsPort;

    @Override
    public UserSettingsDto getUserSettings(GetUserSettingsQuery query) {
        UserSettings settings = userSettingsPort.find(query.user()).orElseGet(() -> UserSettings.defaults(query.user()));
        VoiceChoice voice = voiceChoicesPort.getVoiceChoices().chosenOrDefault(settings.voiceId());
        return UserSettingsDto.from(settings, voice);
    }
}
