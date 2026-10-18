package io.github.phs314.quicktts.bot.device.application.port.in.command;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;

/**
 * @param owner 연결 코드를 받을 디스코드 사용자
 */
public record IssuePairingCodeCommand(DiscordUserId owner) {
}
