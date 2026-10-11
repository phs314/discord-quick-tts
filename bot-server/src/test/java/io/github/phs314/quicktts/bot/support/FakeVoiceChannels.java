package io.github.phs314.quicktts.bot.support;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API 통합 테스트에서 디스코드 음성 상태 대신 쓰는 대역. 사용자를 음성 채널에 넣어 두면 그 채널에 사람이 있는 것으로 본다.
 * 요청은 웹 서버 스레드에서 오므로 여러 스레드에서 읽고 써도 된다.
 */
public class FakeVoiceChannels implements VoiceChannelLocatorPort {

    private final Map<DiscordUserId, VoiceChannelDetails> users = new ConcurrentHashMap<>();

    /** 사용자가 그 음성 채널에 들어간다. */
    public void enter(DiscordUserId user, VoiceChannelDetails details) {
        users.put(user, details);
    }

    public void reset() {
        users.clear();
    }

    @Override
    public Optional<VoiceChannelDetails> findCurrentChannel(DiscordUserId user) {
        return Optional.ofNullable(users.get(user));
    }

    @Override
    public Optional<VoiceChannelDetails> findDetails(VoiceChannel channel) {
        return users.values().stream().filter(details -> details.channel().equals(channel)).findFirst();
    }

    @Override
    public boolean hasPeople(VoiceChannel channel) {
        return findDetails(channel).isPresent();
    }
}
