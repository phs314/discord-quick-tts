package io.github.phs314.quicktts.bot.speech.application.port.in.command;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;

/**
 * @param guildId 봇이 나간 디스코드 서버
 */
public record ClearBotVoiceStateCommand(GuildId guildId) {
}
