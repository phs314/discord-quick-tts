package io.github.phs314.quicktts.bot.device.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.device.application.port.in.command.UnlinkDeviceCommand;
import io.github.phs314.quicktts.bot.device.application.port.in.query.AuthenticateDeviceQuery;
import io.github.phs314.quicktts.bot.device.application.port.in.query.GetDevicesQuery;
import io.github.phs314.quicktts.bot.device.application.port.out.FakeDevicePort;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UnlinkDeviceServiceTest {

    private static final DiscordUserId USER = new DiscordUserId(42L);
    private static final DiscordUserId OTHER = new DiscordUserId(7L);
    private static final Instant NOW = Instant.parse("2026-10-05T00:00:00Z");
    private static final DeviceName PC = new DeviceName("현수-PC");

    private final FakeDevicePort devices = new FakeDevicePort();
    private final GetDevicesService getDevicesService = new GetDevicesService(devices);
    private final UnlinkDeviceService service = new UnlinkDeviceService(devices);

    @Test
    void 해제한_PC_의_토큰은_더_이상_인증되지_않는다() {
        DeviceToken token = devices.registered(USER, PC, NOW);
        DeviceId id = getDevicesService.getDevices(new GetDevicesQuery(USER)).getFirst().id();

        assertThat(service.unlink(new UnlinkDeviceCommand(USER, id))).isTrue();
        AuthenticateDeviceQuery query = new AuthenticateDeviceQuery(token.value());
        assertThat(new AuthenticateDeviceService(devices).authenticate(query)).isEmpty();
    }

    @Test
    void 다른_사람의_PC_는_해제할_수_없다() {
        devices.registered(OTHER, PC, NOW);
        DeviceId othersId = getDevicesService.getDevices(new GetDevicesQuery(OTHER)).getFirst().id();

        assertThat(service.unlink(new UnlinkDeviceCommand(USER, othersId))).isFalse();
        assertThat(getDevicesService.getDevices(new GetDevicesQuery(OTHER))).hasSize(1);
    }
}
