package io.github.phs314.quicktts.bot.device.adapter.in.discord;

import io.github.phs314.quicktts.bot.device.application.port.in.dto.IssuedPairingCodeDto;
import io.github.phs314.quicktts.bot.device.application.port.in.usecase.IssuePairingCodeUseCase;
import io.github.phs314.quicktts.bot.shared.adapter.in.discord.DiscordSlashCommand;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;

/**
 * 디스코드 {@code /연결} 명령. 명령을 친 사람에게만 보이는 연결 코드를 보내 준다.
 * 누가 쳤는지는 디스코드가 보증하므로 사용자가 자기 ID 를 직접 입력할 필요가 없다.
 */
@Component
@RequiredArgsConstructor
public class LinkCommandListener extends ListenerAdapter implements DiscordSlashCommand {

    private static final String COMMAND_NAME = "link";

    private final IssuePairingCodeUseCase issuePairingCode;

    @Override
    public SlashCommandData definition() {
        return Commands.slash(COMMAND_NAME, "Get a code to link the Quick TTS desktop app")
                .setNameLocalization(DiscordLocale.KOREAN, "연결")
                .setDescriptionLocalization(DiscordLocale.KOREAN, "Quick TTS 데스크톱 앱을 연결할 코드를 받습니다");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!COMMAND_NAME.equals(event.getName())) {
            return;
        }
        IssuedPairingCodeDto issued = issuePairingCode.issue(new DiscordUserId(event.getUser().getIdLong()));
        event.reply("""
                        연결 코드: **%s**
                        Quick TTS 데스크톱 앱에 %d분 안에 입력해 주세요. 이 코드는 한 번만 쓸 수 있습니다.
                        연결한 PC 를 끊으려면 `/연결해제` 를 쓰세요."""
                        .formatted(issued.code().display(), issued.validFor().toMinutes()))
                .setEphemeral(true)
                .queue();
    }
}
