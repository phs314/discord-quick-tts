package io.github.phs314.quicktts.bot.device.application.service;

import io.github.phs314.quicktts.bot.device.application.port.in.DeviceDto;
import io.github.phs314.quicktts.bot.device.application.port.in.ManageOwnDevicesUseCase;
import io.github.phs314.quicktts.bot.device.application.port.out.DevicePort;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManageOwnDevicesService implements ManageOwnDevicesUseCase {

    private final DevicePort devicePort;

    @Override
    public List<DeviceDto> listDevices(DiscordUserId owner) {
        return devicePort.findByOwner(owner).stream().map(DeviceDto::from).toList();
    }

    @Override
    public boolean unlink(DiscordUserId owner, DeviceId id) {
        return devicePort.deleteByIdAndOwner(id, owner);
    }

    @Override
    public int unlinkAll(DiscordUserId owner) {
        return devicePort.deleteAllByOwner(owner);
    }
}
