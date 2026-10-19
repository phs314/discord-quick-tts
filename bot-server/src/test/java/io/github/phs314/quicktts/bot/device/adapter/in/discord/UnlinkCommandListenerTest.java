package io.github.phs314.quicktts.bot.device.adapter.in.discord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.phs314.quicktts.bot.device.application.port.in.ManageOwnDevicesUseCase;
import io.github.phs314.quicktts.bot.device.application.port.in.command.UnlinkAllDevicesCommand;
import io.github.phs314.quicktts.bot.device.application.port.in.command.UnlinkDeviceCommand;
import io.github.phs314.quicktts.bot.device.application.port.in.dto.DeviceDto;
import io.github.phs314.quicktts.bot.device.application.port.in.query.GetDevicesQuery;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UnlinkCommandListenerTest {

    private static final DiscordUserId OWNER = new DiscordUserId(42L);

    private final ManageOwnDevicesUseCase manageOwnDevicesUseCase = mock(ManageOwnDevicesUseCase.class);
    private final UnlinkCommandListener listener = new UnlinkCommandListener(manageOwnDevicesUseCase);

    @Test
    void 연결된_PC_가_없으면_없다고만_답한다() {
        ReplyCallbackAction reply = mock(ReplyCallbackAction.class, RETURNS_SELF);
        SlashCommandInteractionEvent event = slashCommand(reply);
        when(manageOwnDevicesUseCase.getDevices(new GetDevicesQuery(OWNER))).thenReturn(List.of());

        listener.onSlashCommandInteraction(event);

        verify(event).reply("연결된 PC 가 없습니다.");
        verify(reply, never()).addComponents(any(MessageTopLevelComponent[].class));
        verify(reply).queue();
    }

    @Test
    void 메뉴에는_PC_를_24대까지_넣고_마지막에_모두_해제를_둔다() {
        ReplyCallbackAction reply = mock(ReplyCallbackAction.class, RETURNS_SELF);
        SlashCommandInteractionEvent event = slashCommand(reply);
        List<DeviceDto> devices = IntStream.rangeClosed(1, 30).mapToObj(UnlinkCommandListenerTest::device).toList();
        when(manageOwnDevicesUseCase.getDevices(new GetDevicesQuery(OWNER))).thenReturn(devices);

        listener.onSlashCommandInteraction(event);

        verify(event).reply("연결된 PC 30대");
        List<SelectOption> options = menuOptions(reply);
        assertThat(options).hasSize(25);
        assertThat(options.getFirst().getValue()).isEqualTo("device-1");
        assertThat(options.get(23).getValue()).isEqualTo("device-24");
        assertThat(options.getLast().getLabel()).isEqualTo("모두 해제");
        assertThat(options.getLast().getDescription()).isEqualTo("연결된 PC 30대를 모두 끊습니다");
    }

    @Test
    void 다른_명령에는_답하지_않는다() {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);
        when(event.getName()).thenReturn("voice");

        listener.onSlashCommandInteraction(event);

        verify(event, never()).reply(anyString());
    }

    @Test
    void 모두_해제를_고르면_끊은_PC_수를_알린다() {
        MessageEditCallbackAction edit = mock(MessageEditCallbackAction.class, RETURNS_SELF);
        when(manageOwnDevicesUseCase.unlinkAll(new UnlinkAllDevicesCommand(OWNER))).thenReturn(3);

        StringSelectInteractionEvent event = selected("*", edit);

        listener.onStringSelectInteraction(event);

        verify(event).editMessage("PC 3대의 연결을 끊었습니다.");
        verifyMenuClosed(edit);
    }

    @Test
    void 고른_PC_의_연결을_끊는다() {
        MessageEditCallbackAction edit = mock(MessageEditCallbackAction.class, RETURNS_SELF);
        when(manageOwnDevicesUseCase.unlink(new UnlinkDeviceCommand(OWNER, new DeviceId("device-1")))).thenReturn(true);

        StringSelectInteractionEvent event = selected("device-1", edit);

        listener.onStringSelectInteraction(event);

        verify(event).editMessage("연결을 끊었습니다. 그 PC 에서 다시 쓰려면 `/연결` 로 코드를 받으세요.");
        verifyMenuClosed(edit);
    }

    @Test
    void 메뉴를_띄운_사이에_이미_끊긴_PC_면_그렇게_알린다() {
        MessageEditCallbackAction edit = mock(MessageEditCallbackAction.class, RETURNS_SELF);
        when(manageOwnDevicesUseCase.unlink(new UnlinkDeviceCommand(OWNER, new DeviceId("device-1")))).thenReturn(false);

        StringSelectInteractionEvent event = selected("device-1", edit);

        listener.onStringSelectInteraction(event);

        verify(event).editMessage("이미 끊긴 PC 입니다.");
        verifyMenuClosed(edit);
    }

    private static SlashCommandInteractionEvent slashCommand(ReplyCallbackAction reply) {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, RETURNS_DEEP_STUBS);
        when(event.getName()).thenReturn("unlink");
        when(event.getUser().getIdLong()).thenReturn(OWNER.value());
        when(event.reply(anyString())).thenReturn(reply);
        return event;
    }

    private static StringSelectInteractionEvent selected(String value, MessageEditCallbackAction edit) {
        StringSelectInteractionEvent event = mock(StringSelectInteractionEvent.class, RETURNS_DEEP_STUBS);
        when(event.getComponentId()).thenReturn("quicktts:unlink");
        when(event.getUser().getIdLong()).thenReturn(OWNER.value());
        when(event.getValues()).thenReturn(List.of(value));
        when(event.editMessage(anyString())).thenReturn(edit);
        return event;
    }

    private static DeviceDto device(int number) {
        return new DeviceDto(new DeviceId("device-" + number), new DeviceName("PC " + number),
                Instant.parse("2026-10-18T00:00:00Z"));
    }

    private static List<SelectOption> menuOptions(ReplyCallbackAction reply) {
        ArgumentCaptor<MessageTopLevelComponent> row = ArgumentCaptor.forClass(MessageTopLevelComponent.class);
        verify(reply).addComponents(row.capture());
        StringSelectMenu menu = (StringSelectMenu) ((ActionRow) row.getValue()).getComponents().getFirst();
        return menu.getOptions();
    }

    /** 고른 뒤에는 메뉴를 지워서 다시 고를 수 없게 한다. */
    private static void verifyMenuClosed(MessageEditCallbackAction edit) {
        verify(edit).setComponents(List.of());
        verify(edit).queue();
    }
}
