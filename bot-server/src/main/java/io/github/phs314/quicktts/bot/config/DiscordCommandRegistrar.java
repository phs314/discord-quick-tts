package io.github.phs314.quicktts.bot.config;

import io.github.phs314.quicktts.bot.shared.adapter.in.discord.DiscordSlashCommand;
import jakarta.annotation.PostConstruct;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Component;

/**
 * 모든 {@link DiscordSlashCommand} 를 모아 디스코드에 한 번에 등록하고 이벤트를 받게 한다.
 */
@Component
public class DiscordCommandRegistrar {

    private final JDA jda;
    private final List<DiscordSlashCommand> commands;

    public DiscordCommandRegistrar(JDA jda, List<DiscordSlashCommand> commands) {
        this.jda = jda;
        this.commands = commands;
    }

    @PostConstruct
    void register() {
        commands.forEach(jda::addEventListener);
        jda.updateCommands()
                .addCommands(commands.stream().map(DiscordSlashCommand::definition).toList())
                .queue();
    }
}
