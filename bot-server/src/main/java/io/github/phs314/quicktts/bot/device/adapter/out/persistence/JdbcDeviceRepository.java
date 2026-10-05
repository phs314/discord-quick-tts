package io.github.phs314.quicktts.bot.device.adapter.out.persistence;

import io.github.phs314.quicktts.bot.device.application.port.out.DeviceRepository;
import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.shared.domain.DiscordUserId;
import java.sql.Timestamp;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * 등록된 기기를 {@code device} 테이블에 저장한다. 스키마는 {@code schema.sql} 에 있다.
 */
@Component
public class JdbcDeviceRepository implements DeviceRepository {

    private final JdbcClient jdbcClient;

    public JdbcDeviceRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void save(Device device) {
        jdbcClient.sql("""
                        insert into device (token_hash, discord_user_id, registered_at)
                        values (:tokenHash, :discordUserId, :registeredAt)
                        """)
                .param("tokenHash", device.tokenHash())
                .param("discordUserId", device.owner().value())
                .param("registeredAt", Timestamp.from(device.registeredAt()))
                .update();
    }

    @Override
    public Optional<Device> findByTokenHash(String tokenHash) {
        return jdbcClient.sql("""
                        select token_hash, discord_user_id, registered_at
                        from device
                        where token_hash = :tokenHash
                        """)
                .param("tokenHash", tokenHash)
                .query((rs, rowNum) -> new Device(
                        rs.getString("token_hash"),
                        new DiscordUserId(rs.getLong("discord_user_id")),
                        rs.getTimestamp("registered_at").toInstant()))
                .optional();
    }
}
