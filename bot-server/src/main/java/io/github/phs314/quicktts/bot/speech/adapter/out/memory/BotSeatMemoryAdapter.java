package io.github.phs314.quicktts.bot.speech.adapter.out.memory;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.out.BotSeatPort;
import io.github.phs314.quicktts.bot.speech.domain.BotSeat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/**
 * 봇 자리를 메모리에 둔다. 봇 서버가 다시 켜지면 봇도 음성 채널에서 나가 있으므로 모두 빈 자리에서 시작하면 된다.
 * 빈 자리는 지워서, 봇이 들어갔다 나간 디스코드 서버가 늘어도 메모리에 쌓이지 않는다.
 */
@Component
public class BotSeatMemoryAdapter implements BotSeatPort {

    private final Map<GuildId, BotSeat> seats = new ConcurrentHashMap<>();

    @Override
    public void withLockedSeat(GuildId guildId, Consumer<BotSeat> work) {
        while (true) {
            BotSeat seat = seats.computeIfAbsent(guildId, BotSeat::empty);
            // 자리 객체를 잠금으로 쓴다. 기다리는 사이에 앞 작업이 빈 자리를 지웠으면 새 자리로 다시 잡는다.
            synchronized (seat) {
                if (seats.get(guildId) == seat) {
                    try {
                        work.accept(seat);
                    } finally {
                        if (seat.channel().isEmpty()) {
                            seats.remove(guildId, seat);
                        }
                    }
                    return;
                }
            }
        }
    }

    /** 그 디스코드 서버의 자리를 기억하고 있는지. 빈 자리는 기억하지 않는다. */
    boolean remembers(GuildId guildId) {
        return seats.containsKey(guildId);
    }
}
