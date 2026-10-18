package io.github.phs314.quicktts.bot.device.application.port.in.query;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;

/**
 * @param owner 기기 목록을 볼 사용자
 */
public record GetDevicesQuery(DiscordUserId owner) {
}
