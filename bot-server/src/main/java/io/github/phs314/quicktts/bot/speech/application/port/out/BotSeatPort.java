package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.BotSeat;
import java.util.function.Consumer;

/**
 * 디스코드 서버마다 봇 자리를 기억한다.
 */
public interface BotSeatPort {

    /**
     * 디스코드 서버의 봇 자리를 꺼내 {@code work} 에 넘기고, 끝나면 바뀐 자리를 기억한다. 처음 묻는 디스코드 서버면 빈 자리다.
     * 빈 자리는 기억하지 않으므로, 봇이 들어갔다 나간 디스코드 서버가 늘어도 자리가 쌓이지 않는다.
     * 같은 디스코드 서버의 자리는 한 번에 하나의 {@code work} 만 다루고, 다른 디스코드 서버끼리는 서로 기다리지 않는다.
     */
    void withLockedSeat(GuildId guildId, Consumer<BotSeat> work);
}
