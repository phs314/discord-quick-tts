package io.github.phs314.quicktts.bot.device.adapter.in.web;

import io.github.phs314.quicktts.bot.device.application.port.in.command.RegisterDeviceCommand;
import io.github.phs314.quicktts.bot.device.application.port.in.usecase.RegisterDeviceUseCase;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceToken;
import io.github.phs314.quicktts.common.DeviceRegistrationRequest;
import io.github.phs314.quicktts.common.DeviceRegistrationResponse;
import io.github.phs314.quicktts.common.QuickTtsApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 데스크톱 클라이언트가 연결 코드를 내고 기기 토큰을 받아 가는 곳.
 */
@RestController
@RequiredArgsConstructor
public class DeviceController {

    private final RegisterDeviceUseCase registerDeviceUseCase;

    @PostMapping(QuickTtsApi.DEVICES_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceRegistrationResponse register(@RequestBody DeviceRegistrationRequest request) {
        RegisterDeviceCommand command = DeviceWebMapper.toCommand(request);
        DeviceToken token = registerDeviceUseCase.register(command);
        return DeviceWebMapper.toResponse(token);
    }
}
