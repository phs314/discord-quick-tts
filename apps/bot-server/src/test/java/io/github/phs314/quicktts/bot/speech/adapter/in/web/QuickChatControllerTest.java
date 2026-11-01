package io.github.phs314.quicktts.bot.speech.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.phs314.quicktts.api.ApiErrorCode;
import io.github.phs314.quicktts.api.QuickTtsApi;
import io.github.phs314.quicktts.bot.config.SecurityConfig;
import io.github.phs314.quicktts.bot.identity.application.port.in.AuthenticateDeviceUseCase;
import io.github.phs314.quicktts.bot.identity.application.port.in.query.AuthenticateDeviceQuery;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.exception.AuthorNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.exception.BotCannotJoinVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.exception.VoiceChannelInUseException;
import io.github.phs314.quicktts.bot.speech.application.port.in.FindCurrentUserVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.dto.VoiceChannelDto;
import io.github.phs314.quicktts.bot.speech.application.port.in.query.FindCurrentUserVoiceChannelQuery;
import io.github.phs314.quicktts.bot.speech.domain.vo.ChannelId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * 클라이언트가 안내 문구를 고르는 오류 코드가 응답 본문 맨 위의 code 속성에 실리는지 확인한다.
 * 실제 MVC, JSON 설정과 기기 토큰 인증 필터(ADR 0031)를 올리고, 유스케이스만 Mockito 로 바꾼다.
 * 인증된 요청은 {@code Authorization: Bearer <기기 토큰>} 을 싣고, 그 토큰의 주인은 AUTHOR 다.
 */
@WebMvcTest(QuickChatController.class)
@Import({SecurityConfig.class, SimpleMeterRegistry.class})
class QuickChatControllerTest {

    private static final DiscordUserId AUTHOR = new DiscordUserId(42L);
    private static final String TOKEN = "author-device-token";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    SimpleMeterRegistry meterRegistry;

    @MockitoBean
    SpeakQuickChatUseCase speak;

    @MockitoBean
    FindCurrentUserVoiceChannelUseCase findCurrentUserVoiceChannel;

    @MockitoBean
    AuthenticateDeviceUseCase authenticateDevice;

    @BeforeEach
    void speakerOwnsToken() {
        given(authenticateDevice.authenticate(new AuthenticateDeviceQuery(TOKEN))).willReturn(Optional.of(AUTHOR));
    }

    /** 테스트끼리 스프링 컨텍스트를 같이 쓰므로 지표도 테스트마다 비운다. */
    @BeforeEach
    void clearMetrics() {
        meterRegistry.clear();
    }

    @Test
    void 인증된_사람이_보낸_문장으로_받으면_202() throws Exception {
        sendQuickChat("안녕").andExpect(status().isAccepted());

        ArgumentCaptor<SpeakQuickChatCommand> spoken = ArgumentCaptor.forClass(SpeakQuickChatCommand.class);
        verify(speak).speak(spoken.capture());
        assertThat(spoken.getValue().author()).isEqualTo(AUTHOR);
    }

    @Test
    void 받고_재생_순서에_넣기까지_걸린_시간을_성공으로_잰다() throws Exception {
        sendQuickChat("안녕").andExpect(status().isAccepted());

        assertThat(speakTimerCount("success", QuickChatController.NO_ERROR)).isEqualTo(1);
    }

    @Test
    void 실패하면_오류_코드와_함께_실패로_잰다() throws Exception {
        willThrow(new AuthorNotInVoiceChannelException(AUTHOR)).given(speak).speak(any());
        sendQuickChat("안녕").andExpect(status().isConflict());

        assertThat(speakTimerCount("failure", ApiErrorCode.AUTHOR_NOT_IN_VOICE_CHANNEL)).isEqualTo(1);
    }

    @Test
    void 예상하지_못한_실패는_unexpected_로_잰다() throws Exception {
        willThrow(new IllegalStateException("고장")).given(speak).speak(any());
        sendQuickChat("안녕").andExpect(status().isInternalServerError());

        assertThat(speakTimerCount("failure", QuickChatController.UNEXPECTED_ERROR)).isEqualTo(1);
    }

    @Test
    void 토큰이_없으면_401_과_device_unauthorized() throws Exception {
        mockMvc.perform(quickChat("안녕"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.INVALID_DEVICE_TOKEN));

        verify(speak, never()).speak(any());
    }

    @Test
    void Bearer_가_아닌_인증_헤더면_401_과_device_unauthorized() throws Exception {
        mockMvc.perform(quickChat("안녕").header(HttpHeaders.AUTHORIZATION, "Basic " + TOKEN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.INVALID_DEVICE_TOKEN));

        verify(speak, never()).speak(any());
    }

    @Test
    void 해제됐거나_모르는_토큰이면_401_과_device_unauthorized() throws Exception {
        mockMvc.perform(quickChat("안녕").header(HttpHeaders.AUTHORIZATION, QuickTtsApi.BEARER_PREFIX + "unknown"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.INVALID_DEVICE_TOKEN));

        verify(speak, never()).speak(any());
    }

    @Test
    void 빈_문장이면_400_과_invalid_value() throws Exception {
        sendQuickChat(" ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.INVALID_VALUE))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void 보낸_사람이_음성_채널에_없으면_409_와_speaker_not_in_voice_channel() throws Exception {
        willThrow(new AuthorNotInVoiceChannelException(AUTHOR)).given(speak).speak(any());
        sendQuickChat("안녕")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.AUTHOR_NOT_IN_VOICE_CHANNEL));
    }

    @Test
    void 봇이_다른_채널에서_쓰이고_있으면_409_와_voice_channel_in_use() throws Exception {
        willThrow(new VoiceChannelInUseException(
                new VoiceChannelDto(new VoiceChannel(new GuildId(1L), new ChannelId(3L)), "우리 서버", null, "게임")))
                .given(speak).speak(any());
        sendQuickChat("안녕")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.VOICE_CHANNEL_IN_USE));
    }

    @Test
    void 봇이_보낸_사람의_채널에_들어갈_수_없으면_403_과_bot_cannot_join_voice_channel() throws Exception {
        willThrow(new BotCannotJoinVoiceChannelException(new VoiceChannel(new GuildId(1L), new ChannelId(2L)), null))
                .given(speak).speak(any());
        sendQuickChat("안녕")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.BOT_CANNOT_JOIN_VOICE_CHANNEL));
    }

    @Test
    void JSON_이_깨졌으면_400_과_invalid_request() throws Exception {
        mockMvc.perform(post(QuickTtsApi.QUICK_CHAT_PATH)
                        .header(HttpHeaders.AUTHORIZATION, QuickTtsApi.BEARER_PREFIX + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.INVALID_REQUEST));
    }

    @Test
    void 예상하지_못한_실패면_500_과_unexpected_failure_이고_원인은_싣지_않는다() throws Exception {
        willThrow(new IllegalStateException("내부 사정")).given(speak).speak(any());
        sendQuickChat("안녕")
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.UNEXPECTED_FAILURE))
                .andExpect(jsonPath("$.detail").value(not(containsString("내부 사정"))));
    }

    @Test
    void 내_음성_채널에_없으면_204() throws Exception {
        given(findCurrentUserVoiceChannel.findCurrentUserVoiceChannel(new FindCurrentUserVoiceChannelQuery(AUTHOR))).willReturn(Optional.empty());

        mockMvc.perform(get(QuickTtsApi.MY_VOICE_CHANNEL_PATH)
                        .header(HttpHeaders.AUTHORIZATION, QuickTtsApi.BEARER_PREFIX + TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void 토큰_없이_내_음성_채널을_물으면_401_과_device_unauthorized() throws Exception {
        mockMvc.perform(get(QuickTtsApi.MY_VOICE_CHANNEL_PATH))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.INVALID_DEVICE_TOKEN));

        verify(findCurrentUserVoiceChannel, never()).findCurrentUserVoiceChannel(any());
    }

    private ResultActions sendQuickChat(String text) throws Exception {
        return mockMvc.perform(quickChat(text).header(HttpHeaders.AUTHORIZATION, QuickTtsApi.BEARER_PREFIX + TOKEN));
    }

    private static MockHttpServletRequestBuilder quickChat(String text) {
        return post(QuickTtsApi.QUICK_CHAT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"" + text + "\"}");
    }

    private long speakTimerCount(String outcome, String error) {
        var timer = meterRegistry.find(QuickChatController.SPEAK_METRIC)
                .tags("outcome", outcome, "error", error)
                .timer();
        return timer == null ? 0 : timer.count();
    }
}
