package io.github.phs314.quicktts.bot.guild.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link AllowedGuildJpaEntity} 를 읽고 쓰는 Spring Data 저장소. {@link AllowedGuildPersistenceAdapter} 만 쓴다.
 */
interface AllowedGuildSpringDataRepository extends JpaRepository<AllowedGuildJpaEntity, Long> {

    List<AllowedGuildJpaEntity> findAllByOrderByAllowedAtAscGuildIdAsc();

    /** 지운 줄 수를 돌려준다. 한 문장으로 지워서 엔티티를 먼저 읽지 않는다. */
    @Transactional
    @Modifying
    @Query("delete from AllowedGuildJpaEntity g where g.guildId = :guildId")
    int deleteByGuildId(long guildId);
}
