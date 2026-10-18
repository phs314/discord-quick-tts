package io.github.phs314.quicktts.bot.guild.application.port.in.query;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;

/**
 * @param requester 명령을 쓴 사용자. 봇 주인이어야 한다
 */
public record GetAllowlistQuery(DiscordUserId requester) {
}
