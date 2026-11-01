package io.github.phs314.quicktts.bot.identity.adapter.in.web;

import io.github.phs314.quicktts.api.DeviceRegistrationRequest;
import io.github.phs314.quicktts.api.DeviceRegistrationResponse;
import io.github.phs314.quicktts.bot.identity.application.port.in.command.RegisterDeviceCommand;
import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.identity.domain.vo.PairingCode;

/**
 * {@link DeviceController} 의 웹 요청·응답과 유스케이스 입력·결과를 서로 바꾼다 (ADR 0030).
 */
final class DeviceWebMapper {

    private DeviceWebMapper() {
    }

    static RegisterDeviceCommand toCommand(DeviceRegistrationRequest request) {
        return new RegisterDeviceCommand(new PairingCode(request.pairingCode()), new DeviceName(request.deviceName()));
    }

    static DeviceRegistrationResponse toResponse(DeviceToken token) {
        return new DeviceRegistrationResponse(token.value());
    }
}
