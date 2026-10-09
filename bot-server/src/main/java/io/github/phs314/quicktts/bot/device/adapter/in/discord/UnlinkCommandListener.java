package io.github.phs314.quicktts.bot.device.adapter.in.discord;

import io.github.phs314.quicktts.bot.device.application.port.in.dto.DeviceDto;
import io.github.phs314.quicktts.bot.device.application.port.in.usecase.ManageOwnDevicesUseCase;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.shared.adapter.in.discord.DiscordSlashCommand;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;

/**
 * 디스코드 {@code /연결해제} 명령. 내게 연결된 PC 목록을 메뉴로 보여 주고, 고른 PC 의 연결을 끊는다.
 * 해제된 PC 의 클라이언트는 다음 전송 때 연결 코드를 다시 묻는다.
 */
@Component
@RequiredArgsConstructor
public class UnlinkCommandListener extends ListenerAdapter implements DiscordSlashCommand {

    private static final String COMMAND_NAME = "unlink";
    private static final String MENU_ID = "quicktts:unlink";
    private static final String ALL_DEVICES = "*";
    /** 디스코드 선택 메뉴는 항목을 25개까지 받는다. "모두 해제" 한 칸을 남긴다. */
    private static final int MAX_DEVICE_OPTIONS = 24;
    private static final DateTimeFormatter REGISTERED_AT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 등록").withZone(ZoneId.of("Asia/Seoul"));

    private final ManageOwnDevicesUseCase manageOwnDevices;

    @Override
    public SlashCommandData definition() {
        return Commands.slash(COMMAND_NAME, "Unlink a desktop app connected to your account")
                .setNameLocalization(DiscordLocale.KOREAN, "연결해제")
                .setDescriptionLocalization(DiscordLocale.KOREAN, "내게 연결된 Quick TTS 데스크톱 앱의 연결을 끊습니다");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!COMMAND_NAME.equals(event.getName())) {
            return;
        }
        List<DeviceDto> devices = manageOwnDevices.listDevices(ownerOf(event.getUser().getIdLong()));
        if (devices.isEmpty()) {
            event.reply("연결된 PC 가 없습니다.").setEphemeral(true).queue();
            return;
        }

        StringSelectMenu.Builder menu = StringSelectMenu.create(MENU_ID)
                .setPlaceholder("연결을 끊을 PC 를 고르세요");
        devices.stream().limit(MAX_DEVICE_OPTIONS).forEach(device ->
                menu.addOption(device.name().value(), device.id().value(), REGISTERED_AT.format(device.registeredAt())));
        menu.addOption("모두 해제", ALL_DEVICES, "연결된 PC " + devices.size() + "대를 모두 끊습니다");

        event.reply("연결된 PC " + devices.size() + "대")
                .addComponents(ActionRow.of(menu.build()))
                .setEphemeral(true)
                .queue();
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!MENU_ID.equals(event.getComponentId())) {
            return;
        }
        DiscordUserId owner = ownerOf(event.getUser().getIdLong());
        String selected = event.getValues().getFirst();

        String result;
        if (ALL_DEVICES.equals(selected)) {
            result = "PC " + manageOwnDevices.unlinkAll(owner) + "대의 연결을 끊었습니다.";
        } else if (manageOwnDevices.unlink(owner, new DeviceId(selected))) {
            result = "연결을 끊었습니다. 그 PC 에서 다시 쓰려면 `/연결` 로 코드를 받으세요.";
        } else {
            result = "이미 끊긴 PC 입니다.";
        }
        event.editMessage(result).setComponents(List.of()).queue();
    }

    private static DiscordUserId ownerOf(long discordUserId) {
        return new DiscordUserId(discordUserId);
    }
}
