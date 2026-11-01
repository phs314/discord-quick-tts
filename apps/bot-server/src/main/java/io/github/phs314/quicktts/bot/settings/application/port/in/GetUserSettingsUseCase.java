package io.github.phs314.quicktts.bot.settings.application.port.in;

import io.github.phs314.quicktts.bot.settings.application.port.in.dto.UserSettingsDto;
import io.github.phs314.quicktts.bot.settings.application.port.in.query.GetUserSettingsQuery;

/**
 * 사용자 설정을 본다. {@code /목소리} 와, quick chat 을 읽기 전에 speech 가 부른다.
 */
public interface GetUserSettingsUseCase {

    /** 고른 적이 없거나 고른 목소리가 더 이상 없으면 기본 목소리가 담긴다. */
    UserSettingsDto getUserSettings(GetUserSettingsQuery query);
}
