package io.github.phs314.quicktts.bot.guild.adapter.out.persistence;

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
 * {@code allowed_guild} 테이블의 한 줄. 도메인의 {@code AllowedGuild} 와 따로 두고
 * {@link AllowedGuildPersistenceMapper} 가 옮긴다 (ADR 0038).
 */
@Entity
@Table(name = "allowed_guild")
class AllowedGuildJpaEntity implements Persistable<Long> {

    @Id
    @Column(name = "guild_id")
    private long guildId;

    @Column(name = "allowed_at")
    private Instant allowedAt;

    /**
     * 기본 키를 도메인이 정해서 넣으므로, 새 줄인지는 Spring Data 가 알 수 없다. 매퍼가 도메인에서 만든 엔티티는 새 줄로 보고
     * 바로 insert 한다(같은 키가 있으면 덮어쓰지 않고 실패한다). DB 에서 읽었거나 넣은 뒤에는 새 줄이 아니다.
     */
    @Transient
    private boolean isNew = true;

    protected AllowedGuildJpaEntity() {
    }

    AllowedGuildJpaEntity(long guildId, Instant allowedAt) {
        this.guildId = guildId;
        this.allowedAt = allowedAt;
    }

    long getGuildId() {
        return guildId;
    }

    Instant getAllowedAt() {
        return allowedAt;
    }

    @Override
    public Long getId() {
        return guildId;
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
