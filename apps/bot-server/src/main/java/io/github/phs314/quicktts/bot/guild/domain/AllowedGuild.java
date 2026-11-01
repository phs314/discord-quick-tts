package io.github.phs314.quicktts.bot.guild.domain;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Instant;
import java.util.Objects;

/**
 * 봇 주인이 허용 목록에 넣은 디스코드 서버 하나. 같은 것인지는 {@link #guildId()} 로만 가린다.
 */
public class AllowedGuild {

    private final GuildId guildId;
    /** 허용 목록에 넣은 시각 */
    private final Instant allowedAt;

    /** 저장소에서 읽어 온 것을 되살릴 때 쓴다. 새로 넣을 때는 {@link #allow} 를 쓴다. */
    public AllowedGuild(GuildId guildId, Instant allowedAt) {
        this.guildId = Objects.requireNonNull(guildId);
        this.allowedAt = Objects.requireNonNull(allowedAt);
    }

    public static AllowedGuild allow(GuildId guildId, Instant now) {
        return new AllowedGuild(guildId, now);
    }

    public GuildId guildId() {
        return guildId;
    }

    public Instant allowedAt() {
        return allowedAt;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AllowedGuild other && guildId.equals(other.guildId);
    }

    @Override
    public int hashCode() {
        return guildId.hashCode();
    }

    @Override
    public String toString() {
        return "AllowedGuild[" + guildId.value() + "]";
    }
}
