package io.github.phs314.quicktts.bot.device.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.phs314.quicktts.bot.device.domain.InvalidPairingCodeException;
import io.github.phs314.quicktts.bot.shared.adapter.in.web.ApiExceptionHandler;
import io.github.phs314.quicktts.common.ApiErrorCode;
import io.github.phs314.quicktts.common.QuickTtsApi;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DeviceControllerTest {

    @Test
    void 없거나_만료된_연결_코드면_400_과_invalid_pairing_code() throws Exception {
        DeviceController controller = new DeviceController(command -> {
            throw new InvalidPairingCodeException();
        });
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        mockMvc.perform(post(QuickTtsApi.DEVICES_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pairingCode\":\"ABCD-EFGH\",\"deviceName\":\"내 PC\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.INVALID_PAIRING_CODE));
    }
}
