package io.github.phs314.quicktts.bot.speech.adapter.in.discord;

import io.github.phs314.quicktts.bot.shared.adapter.in.discord.DiscordSlashCommand;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.exception.UnknownVoiceException;
import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.ManageVoiceUseCase;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;

/**
 * 디스코드 {@code /목소리} 명령. 목소리를 고르면 내가 보내는 문장을 그 목소리로 읽는다.
 * 목소리를 고르지 않고 치면 지금 목소리와 고를 수 있는 목록을 보여 준다.
 */
@Component
@RequiredArgsConstructor
public class VoiceCommandListener extends ListenerAdapter implements DiscordSlashCommand {

    private static final String COMMAND_NAME = "voice";
    private static final String VOICE_OPTION = "voice";

    private final ManageVoiceUseCase manageVoice;

    @Override
    public SlashCommandData definition() {
        OptionData voiceOption = new OptionData(OptionType.STRING, VOICE_OPTION, "Voice to read your messages with")
                .setNameLocalization(DiscordLocale.KOREAN, "목소리")
                .setDescriptionLocalization(DiscordLocale.KOREAN, "내 문장을 읽어 줄 목소리");
        manageVoice.voices().forEach(voice -> voiceOption.addChoice(voice.label(), voice.id().value()));

        return Commands.slash(COMMAND_NAME, "Choose the voice that reads your quick chat messages")
                .setNameLocalization(DiscordLocale.KOREAN, "목소리")
                .setDescriptionLocalization(DiscordLocale.KOREAN, "내 quick chat 을 읽어 줄 목소리를 고릅니다")
                .addOptions(voiceOption);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!COMMAND_NAME.equals(event.getName())) {
            return;
        }
        DiscordUserId user = new DiscordUserId(event.getUser().getIdLong());
        OptionMapping chosen = event.getOption(VOICE_OPTION);

        String reply;
        if (chosen == null) {
            reply = currentVoiceMessage(user);
        } else {
            try {
                Voice voice = manageVoice.changeVoice(user, new VoiceId(chosen.getAsString()));
                reply = "목소리를 **" + voice.label() + "** 로 바꿨어요. 다음에 보내는 문장부터 이 목소리로 읽어요.";
            } catch (UnknownVoiceException e) {
                reply = "없는 목소리예요. 목록에서 골라 주세요.";
            }
        }
        event.reply(reply).setEphemeral(true).queue();
    }

    private String currentVoiceMessage(DiscordUserId user) {
        Voice current = manageVoice.currentVoice(user);
        String choices = manageVoice.voices().stream()
                .map(voice -> (voice.equals(current) ? "▶ " : "・ ") + voice.label())
                .collect(Collectors.joining("\n"));
        return "지금 목소리: **" + current.label() + "**\n" + choices
                + "\n\n바꾸려면 `/목소리` 뒤에 목소리를 골라 주세요.";
    }
}
