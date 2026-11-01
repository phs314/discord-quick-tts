package io.github.phs314.quicktts.bot.identity.application.port.in.command;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;

/**
 * @param userId 자기 기기를 모두 끊으려는 사용자
 */
public record UnlinkAllDevicesCommand(DiscordUserId userId) {
}
