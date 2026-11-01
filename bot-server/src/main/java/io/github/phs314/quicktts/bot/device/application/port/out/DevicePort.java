package io.github.phs314.quicktts.bot.device.application.port.out;

import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceTokenHash;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.util.List;
import java.util.Optional;

public interface DevicePort {

    void save(Device device);

    Optional<Device> findByTokenHash(DeviceTokenHash tokenHash);

    /** 등록한 순서대로 돌려준다. */
    List<Device> findByUserId(DiscordUserId userId);

    Optional<Device> findById(DeviceId id);

    /** 지웠으면 true, 이미 없었으면 false. */
    boolean delete(DeviceId id);

    /** 지운 기기 수를 돌려준다. */
    int deleteAllByUserId(DiscordUserId userId);
}
