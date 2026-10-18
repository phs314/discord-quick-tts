package io.github.phs314.quicktts.bot.device.application.service;

import io.github.phs314.quicktts.bot.device.application.port.in.command.UnlinkAllDevicesCommand;
import io.github.phs314.quicktts.bot.device.application.port.in.command.UnlinkDeviceCommand;
import io.github.phs314.quicktts.bot.device.application.port.in.dto.DeviceDto;
import io.github.phs314.quicktts.bot.device.application.port.in.query.GetDevicesQuery;
import io.github.phs314.quicktts.bot.device.application.port.in.usecase.ManageOwnDevicesUseCase;
import io.github.phs314.quicktts.bot.device.application.port.out.DevicePort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManageOwnDevicesService implements ManageOwnDevicesUseCase {

    private final DevicePort devicePort;

    @Override
    public List<DeviceDto> getDevices(GetDevicesQuery query) {
        return devicePort.findByOwner(query.owner()).stream().map(DeviceDto::from).toList();
    }

    @Override
    public boolean unlink(UnlinkDeviceCommand command) {
        // 다른 사람의 기기는 있는지도 알려 주지 않고 없는 기기처럼 다룬다.
        return devicePort.findById(command.deviceId())
                .filter(device -> device.isOwnedBy(command.owner()))
                .map(device -> devicePort.delete(device.id()))
                .orElse(false);
    }

    @Override
    public int unlinkAll(UnlinkAllDevicesCommand command) {
        return devicePort.deleteAllByOwner(command.owner());
    }
}
