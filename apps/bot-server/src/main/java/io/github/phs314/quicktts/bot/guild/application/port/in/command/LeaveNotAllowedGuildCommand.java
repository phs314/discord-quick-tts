package io.github.phs314.quicktts.bot.guild.application.port.in.command;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;

/**
 * @param guildId 봇이 막 초대된 디스코드 서버
 */
public record LeaveNotAllowedGuildCommand(GuildId guildId) {
}
