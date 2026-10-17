package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.BotSeat;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/** 봇 자리 저장소 대신 쓰는 메모리 대역. 한 스레드에서만 쓰므로 잠그지 않는다. */
public class FakeBotSeatPort implements BotSeatPort {

    private final Map<GuildId, BotSeat> seats = new HashMap<>();

    @Override
    public void withLockedSeat(GuildId guildId, Consumer<BotSeat> work) {
        work.accept(seats.computeIfAbsent(guildId, BotSeat::empty));
    }

    /** 그 디스코드 서버에서 봇이 앉아 있는 음성 채널. */
    public Optional<VoiceChannel> channelOf(GuildId guildId) {
        return Optional.ofNullable(seats.get(guildId)).flatMap(BotSeat::channel);
    }
}
