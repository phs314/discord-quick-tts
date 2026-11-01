package io.github.phs314.quicktts.bot.settings.application.port.in.dto;

import io.github.phs314.quicktts.bot.settings.domain.UserSettings;
import io.github.phs314.quicktts.bot.settings.domain.vo.VoiceChoice;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;

/**
 * 사용자 설정을 지금 쓸 값으로 푼 것. 고른 적이 없거나 고른 목소리가 사라졌으면 기본 목소리가 들어 있다.
 * quick chat 을 읽어 주는 speech 도 이것을 받는다.
 *
 * @param userId     설정의 주인
 * @param voiceId    읽을 목소리
 * @param voiceLabel 읽을 목소리의 이름
 */
public record UserSettingsDto(DiscordUserId userId, VoiceId voiceId, String voiceLabel) {

    public static UserSettingsDto from(UserSettings settings, VoiceChoice voice) {
        return new UserSettingsDto(settings.userId(), voice.id(), voice.label());
    }
}
