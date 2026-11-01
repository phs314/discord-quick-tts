package io.github.phs314.quicktts.bot.speech.adapter.out.settings;

import io.github.phs314.quicktts.bot.settings.application.port.in.GetUserSettingsUseCase;
import io.github.phs314.quicktts.bot.settings.application.port.in.query.GetUserSettingsQuery;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import io.github.phs314.quicktts.bot.speech.application.port.out.UserVoicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * settings 에 사용자 설정을 물어 읽을 목소리를 꺼낸다 (ADR 0040).
 */
@Component
@RequiredArgsConstructor
public class UserVoiceSettingsAdapter implements UserVoicePort {

    private final GetUserSettingsUseCase getUserSettingsUseCase;

    @Override
    public VoiceId getVoiceId(DiscordUserId user) {
        return getUserSettingsUseCase.getUserSettings(new GetUserSettingsQuery(user)).voiceId();
    }
}
