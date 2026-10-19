package io.github.phs314.quicktts.bot.device.application.port.in;

import io.github.phs314.quicktts.bot.device.application.port.in.dto.DeviceDto;
import io.github.phs314.quicktts.bot.device.application.port.in.query.GetDevicesQuery;
import java.util.List;

/**
 * 사용자가 자기에게 연결된 PC 를 본다. 다른 사람의 기기는 보이지 않는다.
 */
public interface GetDevicesUseCase {

    List<DeviceDto> getDevices(GetDevicesQuery query);
}
