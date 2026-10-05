package io.github.phs314.quicktts.bot.domain.device;

import io.github.phs314.quicktts.bot.domain.DiscordUserId;
import java.time.Duration;
import java.time.Instant;

/**
 * 연결 코드를 발급받은 사용자와 만료 시각. 한 번 쓰면 사라진다.
 */
public record Pairing(PairingCode code, DiscordUserId owner, Instant expiresAt) {

    public static final Duration VALID_FOR = Duration.ofMinutes(5);

    public static Pairing issue(DiscordUserId owner, Instant now) {
        return new Pairing(PairingCode.generate(), owner, now.plus(VALID_FOR));
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }
}
