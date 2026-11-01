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
 * {@code pairing} 테이블의 한 줄. 도메인의 {@code Pairing} 과 따로 두고 {@link PairingPersistenceMapper} 가 옮긴다 (ADR 0038).
 */
@Entity
@Table(name = "pairing")
class PairingJpaEntity implements Persistable<String> {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "discord_user_id")
    private long discordUserId;

    @Column(name = "expires_at")
    private Instant expiresAt;

    /** 연결 코드는 도메인이 정하므로 새로 만든 엔티티는 바로 insert 한다. 같은 코드가 있으면 덮어쓰지 않고 실패한다. */
    @Transient
    private boolean isNew = true;

    protected PairingJpaEntity() {
    }

    PairingJpaEntity(String code, long discordUserId, Instant expiresAt) {
        this.code = code;
        this.discordUserId = discordUserId;
        this.expiresAt = expiresAt;
    }

    String getCode() {
        return code;
    }

    long getDiscordUserId() {
        return discordUserId;
    }

    Instant getExpiresAt() {
        return expiresAt;
    }

    @Override
    public String getId() {
        return code;
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
