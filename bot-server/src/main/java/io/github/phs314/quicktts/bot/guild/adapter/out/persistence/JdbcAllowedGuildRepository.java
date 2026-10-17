package io.github.phs314.quicktts.bot.guild.adapter.out.persistence;

import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * 허용 목록을 {@code allowed_guild} 테이블에 저장한다. 스키마는 {@code db/migration} 에 있다.
 */
@Component
@RequiredArgsConstructor
public class JdbcAllowedGuildRepository implements AllowedGuildPort {

    private final JdbcClient jdbcClient;

    @Override
    public List<AllowedGuild> findAll() {
        return jdbcClient.sql("select guild_id, allowed_at from allowed_guild order by allowed_at, guild_id")
                .query((rs, rowNum) -> new AllowedGuild(
                        new GuildId(rs.getLong("guild_id")), rs.getTimestamp("allowed_at").toInstant()))
                .list();
    }

    @Override
    public boolean add(AllowedGuild allowedGuild) {
        try {
            jdbcClient.sql("insert into allowed_guild (guild_id, allowed_at) values (:guild, :allowedAt)")
                    .param("guild", allowedGuild.guildId().value())
                    .param("allowedAt", Timestamp.from(allowedGuild.allowedAt()))
                    .update();
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    @Override
    public boolean delete(GuildId guildId) {
        return jdbcClient.sql("delete from allowed_guild where guild_id = :guild")
                .param("guild", guildId.value())
                .update() > 0;
    }
}
