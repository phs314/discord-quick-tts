package io.github.phs314.quicktts.bot.device.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ManageOwnDevicesServiceTest {

    private static final DiscordUserId OWNER = new DiscordUserId(42L);
    private static final DiscordUserId OTHER = new DiscordUserId(7L);
    private static final Instant NOW = Instant.parse("2026-10-05T00:00:00Z");
    private static final DeviceName PC = new DeviceName("현수-PC");

    private final FakeDevicePort devices = new FakeDevicePort();
    private final ManageOwnDevicesService service = new ManageOwnDevicesService(devices);

    @Test
    void 내_기기_목록에는_내가_등록한_PC_만_이름과_함께_보인다() {
        devices.registered(OWNER, PC, NOW);
        devices.registered(OTHER, new DeviceName("남의-PC"), NOW);

        assertThat(service.listDevices(OWNER)).extracting(Device::name).containsExactly(PC);
    }

    @Test
    void 해제한_PC_의_토큰은_더_이상_인증되지_않는다() {
        DeviceToken token = devices.registered(OWNER, PC, NOW);
        DeviceId id = service.listDevices(OWNER).getFirst().id();

        assertThat(service.unlink(OWNER, id)).isTrue();
        assertThat(new AuthenticateDeviceService(devices).authenticate(token.value())).isEmpty();
    }

    @Test
    void 다른_사람의_PC_는_해제할_수_없다() {
        devices.registered(OTHER, PC, NOW);
        DeviceId othersId = service.listDevices(OTHER).getFirst().id();

        assertThat(service.unlink(OWNER, othersId)).isFalse();
        assertThat(service.listDevices(OTHER)).hasSize(1);
    }

    @Test
    void 모두_해제하면_내_PC_만_전부_끊긴다() {
        devices.registered(OWNER, PC, NOW);
        devices.registered(OWNER, new DeviceName("노트북"), NOW.plusSeconds(1));
        devices.registered(OTHER, PC, NOW);

        assertThat(service.unlinkAll(OWNER)).isEqualTo(2);
        assertThat(service.listDevices(OWNER)).isEmpty();
        assertThat(service.listDevices(OTHER)).hasSize(1);
    }
}
