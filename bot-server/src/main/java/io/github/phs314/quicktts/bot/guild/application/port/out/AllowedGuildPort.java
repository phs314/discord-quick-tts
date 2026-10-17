package io.github.phs314.quicktts.bot.guild.application.port.out;

import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.util.List;

/**
 * 허용 목록에 넣은 디스코드 서버를 저장한다.
 */
public interface AllowedGuildPort {

    /** 넣은 순서대로 */
    List<AllowedGuild> findAll();

    /** @return 새로 넣었으면 true, 이미 있었으면 false */
    boolean saveIfAbsent(AllowedGuild allowedGuild);

    /** 모두 함께 넣거나, 하나라도 실패하면 하나도 넣지 않는다. */
    void saveAll(List<AllowedGuild> allowedGuilds);

    /** @return 지웠으면 true, 원래 없었으면 false */
    boolean delete(GuildId guildId);
}
