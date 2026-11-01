package io.github.phs314.quicktts.bot.device.application.service;

import io.github.phs314.quicktts.bot.device.application.port.in.GetDevicesUseCase;
import io.github.phs314.quicktts.bot.device.application.port.in.dto.DeviceDto;
import io.github.phs314.quicktts.bot.device.application.port.in.query.GetDevicesQuery;
import io.github.phs314.quicktts.bot.device.application.port.out.DevicePort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetDevicesService implements GetDevicesUseCase {

    private final DevicePort devicePort;

    @Override
    public List<DeviceDto> getDevices(GetDevicesQuery query) {
        return devicePort.findByUserId(query.userId()).stream().map(DeviceDto::from).toList();
    }
}
