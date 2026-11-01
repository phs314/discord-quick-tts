package io.github.phs314.quicktts.bot.identity.application.port.in.query;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;

/**
 * @param userId 기기 목록을 볼 사용자
 */
public record GetDevicesQuery(DiscordUserId userId) {
}
