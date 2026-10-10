package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 테스트에서 사용자를 음성 채널에 넣고 빼는 디스코드 대역.
 */
class FakeVoiceChannelLocator implements VoiceChannelLocatorPort {

    private final Map<DiscordUserId, VoiceChannelDetails> speakers = new HashMap<>();
    private final Map<VoiceChannel, VoiceChannelDetails> channels = new HashMap<>();
    private final Set<VoiceChannel> channelsWithPeople = new HashSet<>();

    /** 사용자가 그 음성 채널에 들어간다. 채널에 사람이 생긴다. */
    void enter(DiscordUserId user, VoiceChannelDetails details) {
        speakers.put(user, details);
        channels.put(details.channel(), details);
        channelsWithPeople.add(details.channel());
    }

    /** 음성 채널에 있던 사람이 모두 나간다. */
    void empty(VoiceChannel channel) {
        speakers.values().removeIf(details -> details.channel().equals(channel));
        channelsWithPeople.remove(channel);
    }

    @Override
    public Optional<VoiceChannelDetails> findCurrentChannel(DiscordUserId user) {
        return Optional.ofNullable(speakers.get(user));
    }

    @Override
    public Optional<VoiceChannelDetails> findDetails(VoiceChannel channel) {
        return Optional.ofNullable(channels.get(channel));
    }

    @Override
    public boolean hasPeople(VoiceChannel channel) {
        return channelsWithPeople.contains(channel);
    }
}
