package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 디스코드 음성 상태 대신 쓰는 대역. 사용자를 음성 채널에 넣고 빼면 그 채널에 사람이 있는지가 바뀐다.
 * 통합 테스트에서는 웹 서버 스레드가 읽으므로 여러 스레드에서 읽고 써도 된다.
 */
public class FakeVoiceStatePort implements VoiceStatePort {

    private final Map<DiscordUserId, VoiceChannelDetails> speakers = new ConcurrentHashMap<>();
    private final Map<VoiceChannel, VoiceChannelDetails> channels = new ConcurrentHashMap<>();
    private final Set<VoiceChannel> channelsWithPeople = ConcurrentHashMap.newKeySet();

    /** 사용자가 그 음성 채널에 들어간다. 채널에 사람이 생긴다. */
    public void enter(DiscordUserId user, VoiceChannelDetails details) {
        speakers.put(user, details);
        channels.put(details.channel(), details);
        channelsWithPeople.add(details.channel());
    }

    /** 음성 채널에 있던 사람이 모두 나간다. */
    public void empty(VoiceChannel channel) {
        speakers.values().removeIf(details -> details.channel().equals(channel));
        channelsWithPeople.remove(channel);
    }

    public void reset() {
        speakers.clear();
        channels.clear();
        channelsWithPeople.clear();
    }

    @Override
    public Optional<VoiceChannelDetails> findUserVoiceChannel(DiscordUserId user) {
        return Optional.ofNullable(speakers.get(user));
    }

    @Override
    public Optional<VoiceChannelDetails> findChannelDetails(VoiceChannel channel) {
        return Optional.ofNullable(channels.get(channel));
    }

    @Override
    public boolean hasHumanMembers(VoiceChannel channel) {
        return channelsWithPeople.contains(channel);
    }
}
