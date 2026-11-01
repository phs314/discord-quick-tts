package io.github.phs314.quicktts.bot.identity.adapter.in.web;

import io.github.phs314.quicktts.api.DeviceRegistrationRequest;
import io.github.phs314.quicktts.api.DeviceRegistrationResponse;
import io.github.phs314.quicktts.api.QuickTtsApi;
import io.github.phs314.quicktts.bot.config.OpenApiConfig;
import io.github.phs314.quicktts.bot.identity.application.port.in.RegisterDeviceUseCase;
import io.github.phs314.quicktts.bot.identity.application.port.in.command.RegisterDeviceCommand;
import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 데스크톱 클라이언트가 연결 코드를 내고 기기 토큰을 받아 가는 곳.
 */
@Tag(name = "기기", description = "PC 를 디스코드 사용자에게 연결")
@RestController
@RequiredArgsConstructor
public class DeviceController {

    private final RegisterDeviceUseCase registerDeviceUseCase;

    @PostMapping(QuickTtsApi.DEVICES_PATH)
    @Operation(summary = "기기 등록", description = "디스코드 /연결 로 받은 연결 코드를 내고 기기 토큰을 받는다. 토큰 없이 부른다.")
    @SecurityRequirements
    @ApiResponse(responseCode = "201", description = "등록됨. 기기 토큰은 이때 한 번만 준다.")
    @ApiResponse(responseCode = "400", description = "invalid-pairing-code (없거나 만료된 코드), invalid-value (코드나 PC 이름 형식)",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(ref = OpenApiConfig.PROBLEM)))
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceRegistrationResponse register(@RequestBody DeviceRegistrationRequest request) {
        RegisterDeviceCommand command = DeviceWebMapper.toCommand(request);
        DeviceToken token = registerDeviceUseCase.register(command);
        return DeviceWebMapper.toResponse(token);
    }
}
