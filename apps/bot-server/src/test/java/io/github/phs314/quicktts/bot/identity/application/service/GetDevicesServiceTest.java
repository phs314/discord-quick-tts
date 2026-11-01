package io.github.phs314.quicktts.bot.identity.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.identity.application.port.in.dto.DeviceDto;
import io.github.phs314.quicktts.bot.identity.application.port.in.query.GetDevicesQuery;
import io.github.phs314.quicktts.bot.identity.application.port.out.FakeDevicePort;
import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class GetDevicesServiceTest {

    private static final DiscordUserId USER = new DiscordUserId(42L);
    private static final DiscordUserId OTHER = new DiscordUserId(7L);
    private static final Instant NOW = Instant.parse("2026-10-05T00:00:00Z");
    private static final DeviceName PC = new DeviceName("현수-PC");

    private final FakeDevicePort devices = new FakeDevicePort();
    private final GetDevicesService service = new GetDevicesService(devices);

    @Test
    void 내_기기_목록에는_내가_등록한_PC_만_이름과_함께_보인다() {
        devices.registered(USER, PC, NOW);
        devices.registered(OTHER, new DeviceName("남의-PC"), NOW);

        assertThat(service.getDevices(new GetDevicesQuery(USER))).extracting(DeviceDto::name).containsExactly(PC);
    }
}
