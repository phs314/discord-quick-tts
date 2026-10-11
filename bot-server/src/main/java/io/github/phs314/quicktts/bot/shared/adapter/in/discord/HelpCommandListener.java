package io.github.phs314.quicktts.bot.shared.adapter.in.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;

/**
 * 디스코드 {@code /도움말} 명령. 처음 쓰는 사람이 혼자 시작할 수 있게 시작 순서와 명령 목록을 명령을 친 사람에게만 보여 준다.
 * 어느 컨텍스트의 규칙도 쓰지 않는 고정 안내라서 입력 포트 없이 공유 어댑터에 둔다.
 */
@Component
public class HelpCommandListener extends ListenerAdapter implements DiscordSlashCommand {

    private static final String COMMAND_NAME = "help";

    private static final String DOWNLOAD_URL = "https://github.com/phs314/discord-quick-tts/releases/latest";

    static final String HELP_MESSAGE = """
            **Quick TTS 시작하기**
            1. PC 앱 설치: <%s> 에서 `QuickTTS-*.exe` 를 받아 실행합니다.
            2. 연결: 여기서 `/연결` 을 입력하고, 받은 코드를 앱이 처음 켜질 때 붙여 넣습니다.
            3. 말하기: 음성 채널에 들어가 `Ctrl+Shift+Space` 를 누르고, 문장을 쓴 뒤 Enter 를 칩니다. 봇이 들어와서 읽어 줍니다.

            **명령**
            ・ `/연결`: PC 앱을 연결할 코드를 받습니다.
            ・ `/연결해제`: 연결한 PC 를 끊습니다.
            ・ `/목소리`: 내 문장을 읽어 줄 목소리를 고릅니다.
            ・ `/도움말`: 이 안내를 다시 봅니다.

            단축키는 작업 표시줄 트레이의 Quick TTS 아이콘 → **Change Shortcut...** 에서 바꿉니다.
            같은 서버에서 봇이 이미 다른 음성 채널에서 읽고 있으면, 그 채널이 빌 때까지 기다려 주세요."""
            .formatted(DOWNLOAD_URL);

    @Override
    public SlashCommandData definition() {
        return Commands.slash(COMMAND_NAME, "How to get started with Quick TTS")
                .setNameLocalization(DiscordLocale.KOREAN, "도움말")
                .setDescriptionLocalization(DiscordLocale.KOREAN, "Quick TTS 를 시작하는 방법과 명령 목록을 봅니다");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!COMMAND_NAME.equals(event.getName())) {
            return;
        }
        event.reply(HELP_MESSAGE).setEphemeral(true).queue();
    }
}
