package io.github.phs314.quicktts.bot.device.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import org.springframework.data.domain.Persistable;

/**
 * {@code device} 테이블의 한 줄. 도메인의 {@code Device} 와 따로 두고 {@link DevicePersistenceMapper} 가 옮긴다 (ADR 0038).
 */
@Entity
@Table(name = "device")
class DeviceJpaEntity implements Persistable<String> {

    @Id
    @Column(name = "token_hash")
    private String tokenHash;

    /** 기기 식별자. 기본 키는 토큰 해시라서 JPA 의 id 와 헷갈리지 않게 이름을 바꾼다. */
    @Column(name = "id")
    private String deviceId;

    @Column(name = "discord_user_id")
    private long discordUserId;

    @Column(name = "name")
    private String name;

    @Column(name = "registered_at")
    private Instant registeredAt;

    /**
     * 기본 키를 도메인이 정해서 넣으므로, 새 줄인지는 Spring Data 가 알 수 없다. 매퍼가 도메인에서 만든 엔티티는 새 줄로 보고
     * 바로 insert 한다(같은 키가 있으면 덮어쓰지 않고 실패한다). DB 에서 읽었거나 넣은 뒤에는 새 줄이 아니다.
     */
    @Transient
    private boolean isNew = true;

    protected DeviceJpaEntity() {
    }

    DeviceJpaEntity(String tokenHash, String deviceId, long discordUserId, String name, Instant registeredAt) {
        this.tokenHash = tokenHash;
        this.deviceId = deviceId;
        this.discordUserId = discordUserId;
        this.name = name;
        this.registeredAt = registeredAt;
    }

    String getTokenHash() {
        return tokenHash;
    }

    String getDeviceId() {
        return deviceId;
    }

    long getDiscordUserId() {
        return discordUserId;
    }

    String getName() {
        return name;
    }

    Instant getRegisteredAt() {
        return registeredAt;
    }

    @Override
    public String getId() {
        return tokenHash;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        isNew = false;
    }
}
