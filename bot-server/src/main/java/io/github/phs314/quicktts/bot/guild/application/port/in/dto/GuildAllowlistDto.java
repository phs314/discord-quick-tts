package io.github.phs314.quicktts.bot.guild.application.port.in.dto;

import java.util.List;

/**
 * 허용 목록 전체.
 *
 * @param enabled 허용 목록을 쓰는지. 꺼져 있으면 목록과 상관없이 어느 디스코드 서버에나 들어간다.
 * @param guilds  허용한 디스코드 서버들, 넣은 순서대로
 */
public record GuildAllowlistDto(boolean enabled, List<AllowedGuildDto> guilds) {
}
