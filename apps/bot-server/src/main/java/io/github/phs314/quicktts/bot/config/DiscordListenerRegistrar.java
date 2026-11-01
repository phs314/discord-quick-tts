package io.github.phs314.quicktts.bot.config;

import io.github.phs314.quicktts.bot.shared.adapter.in.discord.DiscordSlashCommand;
import io.github.phs314.quicktts.bot.shared.adapter.in.discord.InteractionErrorHandler;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.EventListener;
import org.springframework.stereotype.Component;

/**
 * 디스코드 이벤트를 받는 빈을 모두 모아 JDA 에 한 번씩 등록한다. 리스너는 스스로 등록하지 않는다.
 * {@link DiscordSlashCommand} 는 명령이 잡지 않은 실패에도 답이 가도록 {@link InteractionErrorHandler} 로 감싸고,
 * 디스코드는 명령 목록을 통째로 바꾸므로 명령 정의도 여기서 한 번에 등록한다.
 */
@Component
@RequiredArgsConstructor
public class DiscordListenerRegistrar {

    private final JDA jda;
    private final List<EventListener> listeners;
    private final List<DiscordSlashCommand> commands;

    @PostConstruct
    void register() {
        listeners.forEach(listener -> jda.addEventListener(listener instanceof DiscordSlashCommand
                ? new InteractionErrorHandler(listener)
                : listener));
        jda.updateCommands()
                .addCommands(commands.stream().map(DiscordSlashCommand::definition).toList())
                .queue();
    }
}
