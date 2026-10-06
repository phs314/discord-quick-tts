package io.github.phs314.quicktts.bot.device.domain;

import io.github.phs314.quicktts.bot.device.domain.vo.PairingCode;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * 연결 코드를 발급받은 사용자와 만료 시각. 한 번 쓰면 사라진다. 같은 발급인지는 {@link #code()} 로만 가린다.
 */
public class Pairing {

    public static final Duration VALID_FOR = Duration.ofMinutes(5);

    private final PairingCode code;
    private final DiscordUserId owner;
    private final Instant expiresAt;

    private Pairing(PairingCode code, DiscordUserId owner, Instant expiresAt) {
        this.code = Objects.requireNonNull(code);
        this.owner = Objects.requireNonNull(owner);
        this.expiresAt = Objects.requireNonNull(expiresAt);
    }

    public static Pairing issue(DiscordUserId owner, Instant now) {
        return new Pairing(PairingCode.generate(), owner, now.plus(VALID_FOR));
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public PairingCode code() {
        return code;
    }

    public DiscordUserId owner() {
        return owner;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Pairing other && code.equals(other.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return "Pairing[owner=" + owner + ", expiresAt=" + expiresAt + "]";
    }
}
