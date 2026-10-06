package io.github.phs314.quicktts.bot.device.application.port.out;

import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository {

    void save(Device device);

    Optional<Device> findByTokenHash(String tokenHash);

    /** 등록한 순서대로 돌려준다. */
    List<Device> findByOwner(DiscordUserId owner);

    /** 그 사용자의 기기일 때만 지운다. 지웠으면 true. */
    boolean deleteByIdAndOwner(DeviceId id, DiscordUserId owner);

    /** 지운 기기 수를 돌려준다. */
    int deleteAllByOwner(DiscordUserId owner);
}
