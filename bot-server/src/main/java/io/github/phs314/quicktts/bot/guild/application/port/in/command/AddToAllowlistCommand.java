package io.github.phs314.quicktts.bot.guild.application.port.in.command;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;

/**
 * @param requester 명령을 쓴 사용자. 봇 주인이어야 한다
 * @param guildId 허용 목록에 넣을 디스코드 서버
 */
public record AddToAllowlistCommand(DiscordUserId requester, GuildId guildId) {
}
