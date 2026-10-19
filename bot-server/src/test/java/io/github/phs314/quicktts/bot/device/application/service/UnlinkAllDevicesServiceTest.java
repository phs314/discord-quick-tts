package io.github.phs314.quicktts.bot.device.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.device.application.port.in.command.UnlinkAllDevicesCommand;
import io.github.phs314.quicktts.bot.device.application.port.in.query.GetDevicesQuery;
import io.github.phs314.quicktts.bot.device.application.port.out.FakeDevicePort;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UnlinkAllDevicesServiceTest {

    private static final DiscordUserId OWNER = new DiscordUserId(42L);
    private static final DiscordUserId OTHER = new DiscordUserId(7L);
    private static final Instant NOW = Instant.parse("2026-10-05T00:00:00Z");
    private static final DeviceName PC = new DeviceName("현수-PC");

    private final FakeDevicePort devices = new FakeDevicePort();
    private final GetDevicesService getDevicesService = new GetDevicesService(devices);
    private final UnlinkAllDevicesService service = new UnlinkAllDevicesService(devices);

    @Test
    void 모두_해제하면_내_PC_만_전부_끊긴다() {
        devices.registered(OWNER, PC, NOW);
        devices.registered(OWNER, new DeviceName("노트북"), NOW.plusSeconds(1));
        devices.registered(OTHER, PC, NOW);

        assertThat(service.unlinkAll(new UnlinkAllDevicesCommand(OWNER))).isEqualTo(2);
        assertThat(getDevicesService.getDevices(new GetDevicesQuery(OWNER))).isEmpty();
        assertThat(getDevicesService.getDevices(new GetDevicesQuery(OTHER))).hasSize(1);
    }
}
