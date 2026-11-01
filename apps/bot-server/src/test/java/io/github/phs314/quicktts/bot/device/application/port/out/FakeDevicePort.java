package io.github.phs314.quicktts.bot.device.application.port.out;

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

/** 기기 저장소 대신 쓰는 메모리 대역. */
public class FakeDevicePort implements DevicePort {

    private final Map<DeviceTokenHash, Device> byHash = new HashMap<>();

    /** 연결 코드를 거치지 않고 기기를 바로 등록해 두고, 그 기기 토큰을 돌려준다. */
    public DeviceToken registered(DiscordUserId userId, DeviceName name, Instant now) {
        DeviceToken token = DeviceToken.generate();
        save(Device.register(token, userId, name, now));
        return token;
    }

    /** 지금 저장된 기기들(토큰 해시별). */
    public Map<DeviceTokenHash, Device> saved() {
        return Map.copyOf(byHash);
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
    public List<Device> findByUserId(DiscordUserId userId) {
        return byHash.values().stream()
                .filter(device -> device.userId().equals(userId))
                .sorted(Comparator.comparing(Device::registeredAt))
                .toList();
    }

    @Override
    public Optional<Device> findById(DeviceId id) {
        return byHash.values().stream().filter(device -> device.id().equals(id)).findFirst();
    }

    @Override
    public boolean delete(DeviceId id) {
        return byHash.values().removeIf(device -> device.id().equals(id));
    }

    @Override
    public int deleteAllByUserId(DiscordUserId userId) {
        int before = byHash.size();
        byHash.values().removeIf(device -> device.userId().equals(userId));
        return before - byHash.size();
    }
}
