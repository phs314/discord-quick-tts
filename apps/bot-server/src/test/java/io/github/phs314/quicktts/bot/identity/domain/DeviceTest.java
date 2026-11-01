package io.github.phs314.quicktts.bot.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DeviceTest {

    private static final DiscordUserId USER = new DiscordUserId(42L);
    private static final DiscordUserId OTHER = new DiscordUserId(7L);

    @Test
    void 기기는_등록한_사용자의_것이다() {
        Device device = Device.register(DeviceToken.generate(), USER, new DeviceName("현수-PC"),
                Instant.parse("2026-10-16T00:00:00Z"));

        assertThat(device.belongsTo(USER)).isTrue();
        assertThat(device.belongsTo(OTHER)).isFalse();
    }
}
