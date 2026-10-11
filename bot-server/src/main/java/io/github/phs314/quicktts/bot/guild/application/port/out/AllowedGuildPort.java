package io.github.phs314.quicktts.bot.guild.application.port.out;

import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.util.List;

/**
 * 허용 목록에 넣은 디스코드 서버를 저장한다.
 */
public interface AllowedGuildPort {

    boolean exists(GuildId guild);

    /** 넣은 순서대로 */
    List<AllowedGuild> findAll();

    /** @return 새로 넣었으면 true, 이미 있었으면 false */
    boolean add(AllowedGuild allowedGuild);

    /** @return 지웠으면 true, 원래 없었으면 false */
    boolean delete(GuildId guild);
}
