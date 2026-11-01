package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.BotVoiceState;
import java.util.function.Consumer;

/**
 * 디스코드 서버마다 봇 음성 상태를 기억한다.
 */
public interface BotVoiceStatePort {

    /**
     * 디스코드 서버의 봇 음성 상태를 꺼내 {@code work} 에 넘기고, 끝나면 바뀐 상태를 기억한다. 처음 묻는 디스코드 서버면 채널이 없는 상태다.
     * 채널이 없는 상태는 기억하지 않으므로, 봇이 들어갔다 나간 디스코드 서버가 늘어도 상태가 쌓이지 않는다.
     * 같은 디스코드 서버의 상태는 한 번에 하나의 {@code work} 만 다루고, 다른 디스코드 서버끼리는 서로 기다리지 않는다.
     */
    void withLockedState(GuildId guildId, Consumer<BotVoiceState> work);
}
