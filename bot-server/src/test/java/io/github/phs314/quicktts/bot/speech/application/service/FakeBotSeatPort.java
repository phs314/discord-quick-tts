package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.port.out.BotSeatPort;
import io.github.phs314.quicktts.bot.speech.domain.BotSeat;
import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

class FakeBotSeatPort implements BotSeatPort {

    private final Map<GuildId, BotSeat> seats = new HashMap<>();

    @Override
    public void withSeat(GuildId guildId, Consumer<BotSeat> work) {
        work.accept(seats.computeIfAbsent(guildId, BotSeat::empty));
    }

    Optional<VoiceChannel> channelOf(GuildId guildId) {
        return Optional.ofNullable(seats.get(guildId)).flatMap(BotSeat::channel);
    }
}
