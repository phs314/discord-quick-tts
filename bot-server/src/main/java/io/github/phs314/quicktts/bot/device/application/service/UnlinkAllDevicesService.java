package io.github.phs314.quicktts.bot.device.application.service;

import io.github.phs314.quicktts.bot.device.application.port.in.UnlinkAllDevicesUseCase;
import io.github.phs314.quicktts.bot.device.application.port.in.command.UnlinkAllDevicesCommand;
import io.github.phs314.quicktts.bot.device.application.port.out.DevicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnlinkAllDevicesService implements UnlinkAllDevicesUseCase {

    private final DevicePort devicePort;

    @Override
    @Transactional
    public int unlinkAll(UnlinkAllDevicesCommand command) {
        return devicePort.deleteAllByOwner(command.owner());
    }
}
