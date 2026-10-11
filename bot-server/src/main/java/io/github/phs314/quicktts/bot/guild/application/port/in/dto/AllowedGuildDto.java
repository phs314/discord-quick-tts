package io.github.phs314.quicktts.bot.guild.application.port.in.dto;

import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Instant;

/**
 * 허용 목록의 디스코드 서버 하나.
 *
 * @param guildId   디스코드 서버
 * @param allowedAt 허용 목록에 넣은 시각
 */
public record AllowedGuildDto(GuildId guildId, Instant allowedAt) {

    public static AllowedGuildDto from(AllowedGuild allowedGuild) {
        return new AllowedGuildDto(allowedGuild.guildId(), allowedGuild.allowedAt());
    }
}
