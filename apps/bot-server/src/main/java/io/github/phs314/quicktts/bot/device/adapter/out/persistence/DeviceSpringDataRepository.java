package io.github.phs314.quicktts.bot.device.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link DeviceJpaEntity} 를 읽고 쓰는 Spring Data 저장소. {@link DevicePersistenceAdapter} 만 쓴다.
 */
interface DeviceSpringDataRepository extends JpaRepository<DeviceJpaEntity, String> {

    Optional<DeviceJpaEntity> findByDeviceId(String deviceId);

    List<DeviceJpaEntity> findByDiscordUserIdOrderByRegisteredAt(long discordUserId);

    /** 지운 줄 수를 돌려준다. 한 문장으로 지워서 엔티티를 먼저 읽지 않는다. */
    @Transactional
    @Modifying
    @Query("delete from DeviceJpaEntity d where d.deviceId = :deviceId")
    int deleteByDeviceId(String deviceId);

    @Transactional
    @Modifying
    @Query("delete from DeviceJpaEntity d where d.discordUserId = :discordUserId")
    int deleteByDiscordUserId(long discordUserId);
}
