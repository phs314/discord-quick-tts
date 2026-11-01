package io.github.phs314.quicktts.bot.speech.domain;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 디스코드 서버 하나에서 봇이 들어가 있거나 들어가는 중인 음성 채널 (디스코드 API 의 Voice State). 봇은 디스코드 서버마다 음성 채널 하나에만 들어갈 수 있다.
 * 봇 서버가 들어가기로 정한 순간 채널을 적으므로, 디스코드 음성 연결이 끝나기 전에 온 다른 채널의 문장도 막는다.
 * 같은 상태인지는 {@link #guildId()} 로만 가린다.
 */
public class BotVoiceState {

    private final GuildId guildId;
    private VoiceChannel channel;

    private BotVoiceState(GuildId guildId) {
        this.guildId = Objects.requireNonNull(guildId);
    }

    public static BotVoiceState empty(GuildId guildId) {
        return new BotVoiceState(guildId);
    }

    /**
     * 이 음성 채널에서 읽으러 가도 되는지 (먼저 온 채널 우선). 채널이 없거나, 이미 그 채널이거나,
     * 봇이 있는 채널에 사람이 아무도 없으면 된다.
     *
     * @param hasPeople 음성 채널에 봇 말고 사람이 한 명이라도 있는지
     */
    public boolean canJoin(VoiceChannel requested, Predicate<VoiceChannel> hasPeople) {
        requireSameGuild(requested);
        return channel == null || channel.equals(requested) || !hasPeople.test(channel);
    }

    /**
     * 그 음성 채널에 들어간다. 다른 채널에 있었으면 옮겨 간다.
     *
     * @throws IllegalStateException 같은 디스코드 서버의 다른 채널에 사람이 남아 있어서 옮길 수 없을 때
     */
    public void join(VoiceChannel requested, Predicate<VoiceChannel> hasPeople) {
        if (!canJoin(requested, hasPeople)) {
            throw new IllegalStateException("봇이 같은 디스코드 서버의 다른 음성 채널에서 쓰이고 있습니다: " + channel);
        }
        channel = requested;
    }

    /**
     * 봇이 있는 채널에 사람이 아무도 없으면 나간다.
     *
     * @return 나갔으면 true. 봇을 음성 채널에서 내보내야 한다
     */
    public boolean leaveIfEmpty(Predicate<VoiceChannel> hasPeople) {
        if (channel == null || hasPeople.test(channel)) {
            return false;
        }
        channel = null;
        return true;
    }

    /**
     * 채널에 사람이 있든 없든 채널을 지운다. 봇이 디스코드 서버에서 나가서 그 서버의 음성 채널에 더는 있을 수 없을 때 쓴다.
     */
    public void clear() {
        channel = null;
    }

    /**
     * 디스코드에서 봇이 다른 채널로 옮겨지거나 끊긴 것(디스코드의 Voice State Update)을 반영한다. 지금 채널이 {@code from} 일 때만 바꾸고,
     * 이미 다른 채널이면 봇 서버가 옮겨 가는 중이라 그대로 둔다.
     *
     * @param to 봇이 옮겨진 채널. 끊겼으면 빈 값
     * @return 채널이 없어졌으면 true. 남은 문장을 버려야 한다
     */
    public boolean applyUpdate(VoiceChannel from, Optional<VoiceChannel> to) {
        requireSameGuild(from);
        to.ifPresent(this::requireSameGuild);
        if (!from.equals(channel)) {
            return false;
        }
        channel = to.orElse(null);
        return channel == null;
    }

    public GuildId guildId() {
        return guildId;
    }

    /** 봇이 있거나 들어가는 중인 음성 채널. 비어 있으면 빈 값. */
    public Optional<VoiceChannel> channel() {
        return Optional.ofNullable(channel);
    }

    private void requireSameGuild(VoiceChannel other) {
        if (!other.guildId().equals(guildId)) {
            throw new IllegalArgumentException("다른 디스코드 서버의 음성 채널입니다: " + other);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BotVoiceState other && guildId.equals(other.guildId);
    }

    @Override
    public int hashCode() {
        return guildId.hashCode();
    }

    @Override
    public String toString() {
        return "BotVoiceState[guildId=" + guildId + ", channel=" + channel + "]";
    }
}
