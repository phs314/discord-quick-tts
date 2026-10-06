package io.github.phs314.quicktts.bot.device.adapter.out.persistence;

import io.github.phs314.quicktts.bot.device.application.port.out.DevicePort;
import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * 등록된 기기를 {@code device} 테이블에 저장한다. 스키마는 {@code schema.sql} 에 있다.
 */
@Component
public class JdbcDeviceRepository implements DevicePort {

    private static final String COLUMNS = "id, token_hash, discord_user_id, name, registered_at";

    private final JdbcClient jdbcClient;

    public JdbcDeviceRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void save(Device device) {
        jdbcClient.sql("insert into device (" + COLUMNS + ")"
                        + " values (:id, :tokenHash, :discordUserId, :name, :registeredAt)")
                .param("id", device.id().value())
                .param("tokenHash", device.tokenHash())
                .param("discordUserId", device.owner().value())
                .param("name", device.name().value())
                .param("registeredAt", Timestamp.from(device.registeredAt()))
                .update();
    }

    @Override
    public Optional<Device> findByTokenHash(String tokenHash) {
        return jdbcClient.sql("select " + COLUMNS + " from device where token_hash = :tokenHash")
                .param("tokenHash", tokenHash)
                .query(JdbcDeviceRepository::toDevice)
                .optional();
    }

    @Override
    public List<Device> findByOwner(DiscordUserId owner) {
        return jdbcClient.sql("select " + COLUMNS + " from device"
                        + " where discord_user_id = :owner order by registered_at")
                .param("owner", owner.value())
                .query(JdbcDeviceRepository::toDevice)
                .list();
    }

    @Override
    public boolean deleteByIdAndOwner(DeviceId id, DiscordUserId owner) {
        return jdbcClient.sql("delete from device where id = :id and discord_user_id = :owner")
                .param("id", id.value())
                .param("owner", owner.value())
                .update() > 0;
    }

    @Override
    public int deleteAllByOwner(DiscordUserId owner) {
        return jdbcClient.sql("delete from device where discord_user_id = :owner")
                .param("owner", owner.value())
                .update();
    }

    private static Device toDevice(ResultSet rs, int rowNum) throws SQLException {
        return new Device(
                new DeviceId(rs.getString("id")),
                rs.getString("token_hash"),
                new DiscordUserId(rs.getLong("discord_user_id")),
                new DeviceName(rs.getString("name")),
                rs.getTimestamp("registered_at").toInstant());
    }
}
