package io.github.phs314.quicktts.bot.identity.application.port.in.command;

import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;

/**
 * @param userId 연결을 끊으려는 사용자
 * @param deviceId 끊을 기기
 */
public record UnlinkDeviceCommand(DiscordUserId userId, DeviceId deviceId) {
}
