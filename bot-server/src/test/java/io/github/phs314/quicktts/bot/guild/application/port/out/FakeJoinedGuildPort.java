package io.github.phs314.quicktts.bot.guild.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.util.ArrayList;
import java.util.List;

/** 디스코드 대신 쓰는 대역. 봇이 들어가 있는 디스코드 서버를 들고 있고, 나가면 목록에서 빠지고 {@link #left()} 에 남는다. */
public class FakeJoinedGuildPort implements JoinedGuildPort {

    private final List<GuildId> joined = new ArrayList<>();
    private final List<GuildId> left = new ArrayList<>();

    public FakeJoinedGuildPort(GuildId... joined) {
        this.joined.addAll(List.of(joined));
    }

    /** 봇이 그 디스코드 서버에 초대된다. */
    public void join(GuildId guildId) {
        joined.add(guildId);
    }

    /** 봇이 나간 디스코드 서버들, 나간 순서대로. */
    public List<GuildId> left() {
        return List.copyOf(left);
    }

    @Override
    public List<GuildId> joinedGuilds() {
        return List.copyOf(joined);
    }

    @Override
    public void leaveWithNotice(GuildId guildId) {
        if (joined.remove(guildId)) {
            left.add(guildId);
        }
    }
}
