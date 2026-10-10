package io.github.phs314.quicktts.bot.speech.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.phs314.quicktts.bot.device.application.port.in.usecase.AuthenticateDeviceUseCase;
import io.github.phs314.quicktts.bot.shared.adapter.in.web.DomainExceptionHandler;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.exception.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.exception.VoiceChannelInUseException;
import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import io.github.phs314.quicktts.common.ApiErrorCode;
import io.github.phs314.quicktts.common.QuickChatApi;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * 클라이언트가 안내 문구를 고르는 오류 코드가 응답 본문 맨 위의 code 속성에 실리는지 확인한다.
 */
class QuickChatControllerTest {

    private static final String TOKEN = "good-token";
    private static final DiscordUserId SPEAKER = new DiscordUserId(42L);
    private static final AuthenticateDeviceUseCase AUTHENTICATE =
            raw -> raw.equals(TOKEN) ? Optional.of(SPEAKER) : Optional.empty();

    private SpeakQuickChatUseCase speak = command -> { };

    @Test
    void 문장을_받으면_202() throws Exception {
        sendQuickChat(TOKEN, "안녕").andExpect(status().isAccepted());
    }

    @Test
    void 기기_토큰이_없으면_401_과_device_unauthorized() throws Exception {
        sendQuickChat(null, "안녕")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.DEVICE_UNAUTHORIZED));
    }

    @Test
    void 해제된_기기의_토큰이면_401_과_device_unauthorized() throws Exception {
        sendQuickChat("revoked-token", "안녕")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.DEVICE_UNAUTHORIZED));
    }

    @Test
    void 빈_문장이면_400_과_invalid_value() throws Exception {
        sendQuickChat(TOKEN, " ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.INVALID_VALUE))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void 보낸_사람이_음성_채널에_없으면_409_와_speaker_not_in_voice_channel() throws Exception {
        speak = command -> {
            throw new SpeakerNotInVoiceChannelException(command.speaker());
        };
        sendQuickChat(TOKEN, "안녕")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.SPEAKER_NOT_IN_VOICE_CHANNEL));
    }

    @Test
    void 봇이_다른_채널에서_쓰이고_있으면_423_과_voice_channel_in_use() throws Exception {
        speak = command -> {
            throw new VoiceChannelInUseException(
                    new VoiceChannelDetails(new VoiceChannel(new GuildId(1L), 3L), "우리 서버", null, "게임"));
        };
        sendQuickChat(TOKEN, "안녕")
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.VOICE_CHANNEL_IN_USE));
    }

    @Test
    void 내_음성_채널을_물을_때도_토큰이_없으면_401_과_device_unauthorized() throws Exception {
        mockMvc().perform(get(QuickChatApi.MY_VOICE_CHANNEL_PATH))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.DEVICE_UNAUTHORIZED));
    }

    private ResultActions sendQuickChat(String token, String text) throws Exception {
        var request = post(QuickChatApi.PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"" + text + "\"}");
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, QuickChatApi.BEARER_PREFIX + token);
        }
        return mockMvc().perform(request);
    }

    private MockMvc mockMvc() {
        QuickChatController controller = new QuickChatController(AUTHENTICATE, speak, user -> Optional.empty());
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new SpeechExceptionHandler(), new DomainExceptionHandler())
                .build();
    }
}
