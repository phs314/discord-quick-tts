package io.github.phs314.quicktts.bot.identity.domain;

import io.github.phs314.quicktts.bot.identity.domain.vo.PairingCode;
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
    private final DiscordUserId userId;
    private final Instant expiresAt;

    private Pairing(PairingCode code, DiscordUserId userId, Instant expiresAt) {
        this.code = Objects.requireNonNull(code);
        this.userId = Objects.requireNonNull(userId);
        this.expiresAt = Objects.requireNonNull(expiresAt);
    }

    public static Pairing issue(DiscordUserId userId, Instant now) {
        return new Pairing(PairingCode.generate(), userId, now.plus(VALID_FOR));
    }

    /** 저장소에서 읽어 온 연결을 되살릴 때 쓴다. 새로 발급할 때는 {@link #issue} 를 쓴다. */
    public static Pairing restore(PairingCode code, DiscordUserId userId, Instant expiresAt) {
        return new Pairing(code, userId, expiresAt);
    }

    /**
     * 연결 코드를 써서 이 코드를 받은 사용자를 돌려준다.
     *
     * @throws InvalidPairingCodeException 코드가 만료됐을 때
     */
    public DiscordUserId redeem(Instant now) {
        if (isExpired(now)) {
            throw new InvalidPairingCodeException();
        }
        return userId;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public PairingCode code() {
        return code;
    }

    public DiscordUserId userId() {
        return userId;
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
        return "Pairing[userId=" + userId + ", expiresAt=" + expiresAt + "]";
    }
}
