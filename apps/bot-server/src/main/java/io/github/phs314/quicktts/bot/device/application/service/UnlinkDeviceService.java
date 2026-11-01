package io.github.phs314.quicktts.bot.device.application.service;

import io.github.phs314.quicktts.bot.device.application.port.in.UnlinkDeviceUseCase;
import io.github.phs314.quicktts.bot.device.application.port.in.command.UnlinkDeviceCommand;
import io.github.phs314.quicktts.bot.device.application.port.out.DevicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnlinkDeviceService implements UnlinkDeviceUseCase {

    private final DevicePort devicePort;

    @Override
    @Transactional
    public boolean unlink(UnlinkDeviceCommand command) {
        // 다른 사람의 기기는 있는지도 알려 주지 않고 없는 기기처럼 다룬다.
        return devicePort.findById(command.deviceId())
                .filter(device -> device.belongsTo(command.userId()))
                .map(device -> devicePort.delete(device.id()))
                .orElse(false);
    }
}
