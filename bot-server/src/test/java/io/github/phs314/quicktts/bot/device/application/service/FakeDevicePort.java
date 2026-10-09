package io.github.phs314.quicktts.bot.device.application.service;

import io.github.phs314.quicktts.bot.device.application.port.out.DevicePort;
import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceTokenHash;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class FakeDevicePort implements DevicePort {

    final Map<DeviceTokenHash, Device> byHash = new HashMap<>();

    /** 연결 코드를 거치지 않고 기기를 바로 등록해 두고, 그 기기 토큰을 돌려준다. */
    DeviceToken registered(DiscordUserId owner, DeviceName name, Instant now) {
        DeviceToken token = DeviceToken.generate();
        save(Device.register(token, owner, name, now));
        return token;
    }

    @Override
    public void save(Device device) {
        byHash.put(device.tokenHash(), device);
    }

    @Override
    public Optional<Device> findByTokenHash(DeviceTokenHash tokenHash) {
        return Optional.ofNullable(byHash.get(tokenHash));
    }

    @Override
    public List<Device> findByOwner(DiscordUserId owner) {
        return byHash.values().stream()
                .filter(device -> device.owner().equals(owner))
                .sorted(Comparator.comparing(Device::registeredAt))
                .toList();
    }

    @Override
    public boolean deleteByIdAndOwner(DeviceId id, DiscordUserId owner) {
        return byHash.values().removeIf(device -> device.id().equals(id) && device.owner().equals(owner));
    }

    @Override
    public int deleteAllByOwner(DiscordUserId owner) {
        int before = byHash.size();
        byHash.values().removeIf(device -> device.owner().equals(owner));
        return before - byHash.size();
    }
}
