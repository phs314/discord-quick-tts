package io.github.phs314.quicktts.bot.speech.adapter.out.persistence;

import io.github.phs314.quicktts.bot.speech.application.port.out.BotSeatPort;
import io.github.phs314.quicktts.bot.speech.domain.BotSeat;
import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/**
 * 봇 자리를 메모리에 둔다. 봇 서버가 다시 켜지면 봇도 음성 채널에서 나가 있으므로 모두 빈 자리에서 시작하면 된다.
 */
@Component
public class InMemoryBotSeatRepository implements BotSeatPort {

    private final Map<GuildId, BotSeat> seats = new ConcurrentHashMap<>();

    @Override
    public void withSeat(GuildId guildId, Consumer<BotSeat> work) {
        BotSeat seat = seats.computeIfAbsent(guildId, BotSeat::empty);
        // 자리는 지우지 않으므로 디스코드 서버마다 같은 객체를 잠금으로 쓸 수 있다.
        synchronized (seat) {
            work.accept(seat);
        }
    }
}
