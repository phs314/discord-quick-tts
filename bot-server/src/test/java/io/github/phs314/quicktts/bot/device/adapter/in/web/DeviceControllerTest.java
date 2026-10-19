package io.github.phs314.quicktts.bot.device.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.phs314.quicktts.bot.config.SecurityConfig;
import io.github.phs314.quicktts.bot.device.application.port.in.AuthenticateDeviceUseCase;
import io.github.phs314.quicktts.bot.device.application.port.in.RegisterDeviceUseCase;
import io.github.phs314.quicktts.bot.device.domain.InvalidPairingCodeException;
import io.github.phs314.quicktts.common.ApiErrorCode;
import io.github.phs314.quicktts.common.QuickTtsApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 기기 등록 오류 코드를 확인한다. 실제 MVC, JSON 설정과 보안 설정(ADR 0031)을 올리므로,
 * 기기 등록이 토큰 없이 열려 있는지도 함께 본다. 유스케이스만 Mockito 로 바꾼다.
 */
@WebMvcTest(DeviceController.class)
@Import(SecurityConfig.class)
class DeviceControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RegisterDeviceUseCase registerDevice;

    /** 보안 설정의 기기 토큰 인증 필터가 쓴다. 이 테스트의 요청은 토큰을 싣지 않는다. */
    @MockitoBean
    AuthenticateDeviceUseCase authenticateDevice;

    @Test
    void 없거나_만료된_연결_코드면_400_과_invalid_pairing_code() throws Exception {
        willThrow(new InvalidPairingCodeException()).given(registerDevice).register(any());

        mockMvc.perform(post(QuickTtsApi.DEVICES_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pairingCode\":\"ABCD-EFGH\",\"deviceName\":\"내 PC\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.INVALID_PAIRING_CODE));
    }
}
