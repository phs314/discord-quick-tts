package io.github.phs314.quicktts.bot.device.application.port.in.command;

import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.device.domain.vo.PairingCode;

/**
 * @param code 디스코드에서 받은 연결 코드
 * @param name 연결할 PC 이름
 */
public record RegisterDeviceCommand(PairingCode code, DeviceName name) {
}
