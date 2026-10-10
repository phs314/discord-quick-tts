package io.github.phs314.quicktts.bot.speech.domain;

import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 디스코드 서버 하나에서 봇이 앉은 음성 채널. 봇은 디스코드 서버마다 음성 채널 하나에만 들어갈 수 있다.
 * 봇 서버가 들어가기로 정한 순간 자리를 잡으므로, 디스코드 음성 연결이 끝나기 전에 온 다른 채널의 문장도 막는다.
 * 같은 자리인지는 {@link #guildId()} 로만 가린다.
 */
public class BotSeat {

    private final GuildId guildId;
    private VoiceChannel channel;

    private BotSeat(GuildId guildId) {
        this.guildId = Objects.requireNonNull(guildId);
    }

    public static BotSeat empty(GuildId guildId) {
        return new BotSeat(guildId);
    }

    /**
     * 이 음성 채널에서 읽으러 가도 되는지 (먼저 온 채널 우선). 자리가 비었거나, 이미 그 채널이거나,
     * 앉은 채널에 사람이 아무도 없으면 된다.
     *
     * @param hasPeople 음성 채널에 봇 말고 사람이 한 명이라도 있는지
     */
    public boolean isFreeFor(VoiceChannel requested, Predicate<VoiceChannel> hasPeople) {
        requireSameGuild(requested);
        return channel == null || channel.equals(requested) || !hasPeople.test(channel);
    }

    /**
     * 그 음성 채널로 자리를 옮긴다.
     *
     * @throws IllegalStateException 같은 디스코드 서버의 다른 채널에 사람이 남아 있어서 옮길 수 없을 때
     */
    public void sitIn(VoiceChannel requested, Predicate<VoiceChannel> hasPeople) {
        if (!isFreeFor(requested, hasPeople)) {
            throw new IllegalStateException("봇이 같은 디스코드 서버의 다른 음성 채널에서 쓰이고 있습니다: " + channel);
        }
        channel = requested;
    }

    /**
     * 앉은 채널에 사람이 아무도 없으면 자리를 비운다.
     *
     * @return 자리를 비웠으면 true. 봇을 음성 채널에서 내보내야 한다
     */
    public boolean vacateIfEmpty(Predicate<VoiceChannel> hasPeople) {
        if (channel == null || hasPeople.test(channel)) {
            return false;
        }
        channel = null;
        return true;
    }

    /**
     * 디스코드에서 봇이 다른 채널로 옮겨지거나 끊긴 것을 자리에 반영한다. 자리가 {@code from} 일 때만 바꾸고,
     * 이미 다른 채널이면 봇 서버가 옮겨 가는 중이라 그대로 둔다.
     *
     * @param to 봇이 옮겨진 채널. 끊겼으면 빈 값
     * @return 자리를 비웠으면 true. 남은 문장을 버려야 한다
     */
    public boolean followBotMove(VoiceChannel from, Optional<VoiceChannel> to) {
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
        return o instanceof BotSeat other && guildId.equals(other.guildId);
    }

    @Override
    public int hashCode() {
        return guildId.hashCode();
    }

    @Override
    public String toString() {
        return "BotSeat[guildId=" + guildId + ", channel=" + channel + "]";
    }
}
