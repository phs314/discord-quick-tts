package io.github.phs314.quicktts.bot.settings.application.port.in.query;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;

/**
 * @param user 설정을 볼 사용자
 */
public record GetUserSettingsQuery(DiscordUserId user) {
}
