package io.github.phs314.quicktts.bot.device.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PairingTest {

    private static final DiscordUserId USER = new DiscordUserId(42L);
    private static final Instant ISSUED_AT = Instant.parse("2026-10-16T00:00:00Z");

    private final Pairing pairing = Pairing.issue(USER, ISSUED_AT);

    @Test
    void 만료_전에_쓰면_코드를_받은_사용자를_돌려준다() {
        assertThat(pairing.redeem(ISSUED_AT.plus(Pairing.VALID_FOR).minusSeconds(1))).isEqualTo(USER);
    }

    @Test
    void 만료된_코드는_쓸_수_없다() {
        assertThatThrownBy(() -> pairing.redeem(ISSUED_AT.plus(Pairing.VALID_FOR)))
                .isInstanceOf(InvalidPairingCodeException.class);
    }
}
