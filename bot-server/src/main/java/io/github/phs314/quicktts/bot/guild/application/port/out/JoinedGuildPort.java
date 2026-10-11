package io.github.phs314.quicktts.bot.guild.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.util.List;

/**
 * 봇이 들어가 있는 디스코드 서버를 보고, 거기서 나간다.
 */
public interface JoinedGuildPort {

    List<GuildId> joinedGuilds();

    /** 허용 목록에 없어서 나간다는 안내를 그 디스코드 서버에 남길 수 있으면 남기고 나간다. 이미 없으면 아무것도 하지 않는다. */
    void leaveNotAllowed(GuildId guild);
}
