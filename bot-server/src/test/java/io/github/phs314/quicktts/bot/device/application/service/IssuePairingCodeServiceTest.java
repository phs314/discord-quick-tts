package io.github.phs314.quicktts.bot.device.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.device.domain.Pairing;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class IssuePairingCodeServiceTest {

    private static final DiscordUserId OWNER = new DiscordUserId(42L);
    private static final Instant NOW = Instant.parse("2026-10-05T00:00:00Z");

    private final FakePairingPort pairings = new FakePairingPort();
    private final IssuePairingCodeService service =
            new IssuePairingCodeService(pairings, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void 발급한_연결_코드는_받은_사용자의_것으로_저장된다() {
        Pairing pairing = service.issue(OWNER);

        assertThat(pairings.byCode).containsOnlyKeys(pairing.code());
        assertThat(pairing.owner()).isEqualTo(OWNER);
        assertThat(pairing.isExpired(NOW)).isFalse();
    }
}
