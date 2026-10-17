package io.github.phs314.quicktts.bot.guild.adapter.out.persistence;

import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
    public boolean saveIfAbsent(AllowedGuild allowedGuild) {
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

    /** 문장 하나로 넣어서, 하나라도 실패하면 DB 가 모두 되돌린다. */
    @Override
    public void saveAll(List<AllowedGuild> allowedGuilds) {
        if (allowedGuilds.isEmpty()) {
            return;
        }
        String rows = IntStream.range(0, allowedGuilds.size())
                .mapToObj(i -> "(:guild" + i + ", :allowedAt" + i + ")")
                .collect(Collectors.joining(", "));
        var statement = jdbcClient.sql("insert into allowed_guild (guild_id, allowed_at) values " + rows);
        for (int i = 0; i < allowedGuilds.size(); i++) {
            statement = statement.param("guild" + i, allowedGuilds.get(i).guildId().value())
                    .param("allowedAt" + i, Timestamp.from(allowedGuilds.get(i).allowedAt()));
        }
        statement.update();
    }

    @Override
    public boolean delete(GuildId guildId) {
        return jdbcClient.sql("delete from allowed_guild where guild_id = :guild")
                .param("guild", guildId.value())
                .update() > 0;
    }
}
