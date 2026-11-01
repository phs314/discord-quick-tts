package io.github.phs314.quicktts.bot.device.application.port.in.dto;

import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import java.time.Instant;

/**
 * 사용자에게 보여 줄 연결된 PC 한 대. 기기 토큰 해시처럼 바깥에 보일 필요 없는 값은 담지 않는다.
 *
 * @param id           연결을 끊을 때 고르는 식별자
 * @param name         PC 이름
 * @param registeredAt 등록 시각
 */
public record DeviceDto(DeviceId id, DeviceName name, Instant registeredAt) {

    public static DeviceDto from(Device device) {
        return new DeviceDto(device.id(), device.name(), device.registeredAt());
    }
}
