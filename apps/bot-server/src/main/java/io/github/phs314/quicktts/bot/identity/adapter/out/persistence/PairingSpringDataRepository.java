package io.github.phs314.quicktts.bot.identity.adapter.out.persistence;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link PairingJpaEntity} 를 읽고 쓰는 Spring Data 저장소. {@link PairingPersistenceAdapter} 만 쓴다.
 */
interface PairingSpringDataRepository extends JpaRepository<PairingJpaEntity, String> {

    /** 지운 줄 수를 돌려준다. 두 요청이 같은 코드를 동시에 써도 한쪽만 1 을 받는다. */
    @Transactional
    @Modifying
    @Query("delete from PairingJpaEntity p where p.code = :code")
    int deleteByCode(String code);

    @Transactional
    @Modifying
    @Query("delete from PairingJpaEntity p where p.expiresAt <= :now")
    int deleteExpired(Instant now);
}
