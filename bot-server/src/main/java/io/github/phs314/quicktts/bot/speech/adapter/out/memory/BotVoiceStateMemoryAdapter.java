package io.github.phs314.quicktts.bot.speech.adapter.out.memory;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.out.BotVoiceStatePort;
import io.github.phs314.quicktts.bot.speech.domain.BotVoiceState;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/**
 * 봇 음성 상태를 메모리에 둔다. 봇 서버가 다시 켜지면 봇도 음성 채널에서 나가 있으므로 모두 채널 없음에서 시작하면 된다.
 * 채널이 없는 상태는 지워서, 봇이 들어갔다 나간 디스코드 서버가 늘어도 메모리에 쌓이지 않는다.
 */
@Component
public class BotVoiceStateMemoryAdapter implements BotVoiceStatePort {

    private final Map<GuildId, BotVoiceState> states = new ConcurrentHashMap<>();

    @Override
    public void withLockedState(GuildId guildId, Consumer<BotVoiceState> work) {
        while (true) {
            BotVoiceState state = states.computeIfAbsent(guildId, BotVoiceState::empty);
            // 상태 객체를 잠금으로 쓴다. 기다리는 사이에 앞 작업이 그 상태를 지웠으면 새 상태로 다시 잡는다.
            synchronized (state) {
                if (states.get(guildId) == state) {
                    try {
                        work.accept(state);
                    } finally {
                        if (state.channel().isEmpty()) {
                            states.remove(guildId, state);
                        }
                    }
                    return;
                }
            }
        }
    }

    /** 그 디스코드 서버의 상태를 기억하고 있는지. 채널이 없는 상태는 기억하지 않는다. */
    boolean remembers(GuildId guildId) {
        return states.containsKey(guildId);
    }
}
