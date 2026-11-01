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

    /** 이 디스코드 서버가 허용 목록에 들어 있는지. 서버 하나를 볼 때 목록 전체를 읽지 않는다. */
    boolean exists(GuildId guildId);

    /** @return 새로 넣었으면 true, 이미 있었으면 false */
    boolean saveIfAbsent(AllowedGuild allowedGuild);

    /** 하나라도 넣지 못하면 예외를 던진다. 이미 넣은 줄은 부르는 유스케이스의 트랜잭션이 되돌린다. */
    void saveAll(List<AllowedGuild> allowedGuilds);

    /** @return 지웠으면 true, 원래 없었으면 false */
    boolean delete(GuildId guildId);
}
