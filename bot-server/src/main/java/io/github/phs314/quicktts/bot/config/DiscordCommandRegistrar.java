package io.github.phs314.quicktts.bot.config;

import io.github.phs314.quicktts.bot.shared.adapter.in.discord.DiscordSlashCommand;
import io.github.phs314.quicktts.bot.shared.adapter.in.discord.FailureReplyingListener;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Component;

/**
 * 모든 {@link DiscordSlashCommand} 를 모아 디스코드에 한 번에 등록하고 이벤트를 받게 한다.
 * 명령이 잡지 않은 실패에도 답이 가도록 {@link FailureReplyingListener} 로 감싼다.
 */
@Component
@RequiredArgsConstructor
public class DiscordCommandRegistrar {

    private final JDA jda;
    private final List<DiscordSlashCommand> commands;

    @PostConstruct
    void register() {
        commands.forEach(command -> jda.addEventListener(new FailureReplyingListener(command)));
        jda.updateCommands()
                .addCommands(commands.stream().map(DiscordSlashCommand::definition).toList())
                .queue();
    }
}
