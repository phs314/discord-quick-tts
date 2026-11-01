package io.github.phs314.quicktts.bot.guild.application.port.in;

import io.github.phs314.quicktts.bot.guild.application.port.in.dto.GuildAllowlistDto;
import io.github.phs314.quicktts.bot.guild.application.port.in.query.GetAllowlistQuery;

/**
 * 봇 주인이 봇을 들일 디스코드 서버 허용 목록을 본다. 봇 주인이 아니면
 * {@link io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException} 을 던진다.
 */
public interface GetAllowlistUseCase {

    GuildAllowlistDto getAllowlist(GetAllowlistQuery query);
}
