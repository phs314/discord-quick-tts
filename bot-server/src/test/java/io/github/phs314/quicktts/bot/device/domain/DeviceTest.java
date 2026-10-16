package io.github.phs314.quicktts.bot.device.domain;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DeviceTest {

    private static final DiscordUserId OWNER = new DiscordUserId(42L);
    private static final DiscordUserId OTHER = new DiscordUserId(7L);

    @Test
    void 기기의_주인은_등록한_사용자뿐이다() {
        Device device = Device.register(DeviceToken.generate(), OWNER, new DeviceName("현수-PC"),
                Instant.parse("2026-10-16T00:00:00Z"));

        assertThat(device.isOwnedBy(OWNER)).isTrue();
        assertThat(device.isOwnedBy(OTHER)).isFalse();
    }
}
