package io.github.phs314.quicktts.bot.guild.domain.vo;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.util.List;
import java.util.Set;

/**
 * 지금의 허용 목록. 봇이 어느 디스코드 서버에 머물러도 되는지는 여기서만 정한다.
 *
 * @param enabled 허용 목록을 쓰는지. 쓰지 않으면 어느 디스코드 서버에나 머문다
 * @param guilds  봇 주인이 허용한 디스코드 서버
 */
public record GuildAllowlist(boolean enabled, Set<GuildId> guilds) {

    public GuildAllowlist {
        guilds = Set.copyOf(guilds);
    }

    /** 봇이 이 디스코드 서버에 머물러도 되는지. */
    public boolean permits(GuildId guild) {
        return !enabled || guilds.contains(guild);
    }

    /** 봇이 들어가 있는 디스코드 서버 중 나가야 하는 곳. */
    public List<GuildId> notPermitted(List<GuildId> joined) {
        return joined.stream().filter(guild -> !permits(guild)).toList();
    }

    /**
     * 처음 채워야 하는지. 허용 목록을 쓰는데 비어 있으면, 봇 주인이 이미 쓰던 디스코드 서버에서 쫓겨나지 않게
     * 봇이 지금 들어가 있는 디스코드 서버를 모두 넣는다.
     */
    public boolean needsFirstFill() {
        return enabled && guilds.isEmpty();
    }
}
