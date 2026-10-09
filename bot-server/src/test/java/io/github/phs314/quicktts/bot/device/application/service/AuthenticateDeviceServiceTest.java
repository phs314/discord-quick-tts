package io.github.phs314.quicktts.bot.device.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuthenticateDeviceServiceTest {

    private static final DiscordUserId OWNER = new DiscordUserId(42L);
    private static final Instant NOW = Instant.parse("2026-10-05T00:00:00Z");

    private final FakeDevicePort devices = new FakeDevicePort();
    private final AuthenticateDeviceService service = new AuthenticateDeviceService(devices);

    @Test
    void 등록된_기기_토큰은_그_기기의_주인으로_인증된다() {
        DeviceToken token = devices.registered(OWNER, new DeviceName("현수-PC"), NOW);

        assertThat(service.authenticate(token.value())).contains(OWNER);
    }

    @Test
    void 등록되지_않은_토큰은_인증되지_않는다() {
        assertThat(service.authenticate(DeviceToken.generate().value())).isEmpty();
    }

    @Test
    void 빈_토큰은_인증되지_않는다() {
        assertThat(service.authenticate(null)).isEmpty();
        assertThat(service.authenticate(" ")).isEmpty();
    }
}
