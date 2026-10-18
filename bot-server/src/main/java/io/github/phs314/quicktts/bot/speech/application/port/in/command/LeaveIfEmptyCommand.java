package io.github.phs314.quicktts.bot.speech.application.port.in.command;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;

/**
 * @param guildId 누군가 음성 채널을 나간 디스코드 서버
 */
public record LeaveIfEmptyCommand(GuildId guildId) {
}
