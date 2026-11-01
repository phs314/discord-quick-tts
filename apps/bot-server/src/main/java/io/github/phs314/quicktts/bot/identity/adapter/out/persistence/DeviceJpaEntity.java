package io.github.phs314.quicktts.bot.identity.adapter.out.persistence;

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
    @Column(name = "id")
    private String id;

    /** 토큰은 다시 발급할 수 있어서 기본 키가 아니라 유일한 값으로 둔다. */
    @Column(name = "token_hash", unique = true)
    private String tokenHash;

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

    DeviceJpaEntity(String id, String tokenHash, long discordUserId, String name, Instant registeredAt) {
        this.id = id;
        this.tokenHash = tokenHash;
        this.discordUserId = discordUserId;
        this.name = name;
        this.registeredAt = registeredAt;
    }

    String getTokenHash() {
        return tokenHash;
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
        return id;
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
