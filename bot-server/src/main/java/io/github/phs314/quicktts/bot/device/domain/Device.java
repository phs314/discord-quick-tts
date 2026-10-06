package io.github.phs314.quicktts.bot.device.domain;

import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Instant;
import java.util.Objects;

/**
 * 디스코드 사용자에게 연결된 PC 한 대. 같은 기기인지는 {@link #id()} 로만 가린다.
 */
public class Device {

    /** 사용자가 기기를 골라 해제할 때 쓰는 식별자 */
    private final DeviceId id;
    /** 기기 토큰의 해시 ({@link DeviceToken#hash()}) */
    private final String tokenHash;
    /** 이 기기로 quick chat 을 보내는 사용자 */
    private final DiscordUserId owner;
    /** 사용자에게 보여 줄 PC 이름 */
    private final DeviceName name;
    /** 등록 시각 */
    private final Instant registeredAt;

    /** 저장소에서 읽어 온 기기를 되살릴 때 쓴다. 새로 등록할 때는 {@link #register} 를 쓴다. */
    public Device(DeviceId id, String tokenHash, DiscordUserId owner, DeviceName name, Instant registeredAt) {
        this.id = Objects.requireNonNull(id);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.owner = Objects.requireNonNull(owner);
        this.name = Objects.requireNonNull(name);
        this.registeredAt = Objects.requireNonNull(registeredAt);
    }

    public static Device register(DeviceToken token, DiscordUserId owner, DeviceName name, Instant now) {
        return new Device(DeviceId.generate(), token.hash(), owner, name, now);
    }

    public DeviceId id() {
        return id;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public DiscordUserId owner() {
        return owner;
    }

    public DeviceName name() {
        return name;
    }

    public Instant registeredAt() {
        return registeredAt;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Device other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Device[id=" + id.value() + ", owner=" + owner + ", name=" + name.value() + "]";
    }
}
