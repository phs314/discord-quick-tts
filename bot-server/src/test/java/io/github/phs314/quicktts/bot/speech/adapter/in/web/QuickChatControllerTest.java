package io.github.phs314.quicktts.bot.speech.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.phs314.quicktts.bot.shared.adapter.in.web.ApiExceptionHandler;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.exception.BotCannotJoinVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.exception.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.exception.VoiceChannelInUseException;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import io.github.phs314.quicktts.common.ApiErrorCode;
import io.github.phs314.quicktts.common.QuickTtsApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * 클라이언트가 안내 문구를 고르는 오류 코드가 응답 본문 맨 위의 code 속성에 실리는지 확인한다.
 * 기기 토큰 인증은 필터가 맡으므로 여기서는 인증을 마친 요청으로 보낸다. 토큰이 없을 때의 401 은 통합 테스트가 본다.
 */
class QuickChatControllerTest {

    private static final DiscordUserId SPEAKER = new DiscordUserId(42L);

    private SpeakQuickChatUseCase speak = command -> { };

    /** 기기 토큰 인증 필터가 SPEAKER 를 알아낸 뒤의 요청으로 보낸다. */
    @BeforeEach
    void signIn() {
        SecurityContextHolder.getContext()
                .setAuthentication(UsernamePasswordAuthenticationToken.authenticated(SPEAKER, null, List.of()));
    }

    @AfterEach
    void signOut() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 인증된_사람이_보낸_문장으로_받으면_202() throws Exception {
        List<SpeakQuickChatCommand> spoken = new ArrayList<>();
        speak = spoken::add;

        sendQuickChat("안녕").andExpect(status().isAccepted());

        assertThat(spoken).extracting(SpeakQuickChatCommand::speaker).containsExactly(SPEAKER);
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
        speak = command -> {
            throw new SpeakerNotInVoiceChannelException(command.speaker());
        };
        sendQuickChat("안녕")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.SPEAKER_NOT_IN_VOICE_CHANNEL));
    }

    @Test
    void 봇이_다른_채널에서_쓰이고_있으면_423_과_voice_channel_in_use() throws Exception {
        speak = command -> {
            throw new VoiceChannelInUseException(
                    new VoiceChannelDetails(new VoiceChannel(new GuildId(1L), 3L), "우리 서버", null, "게임"));
        };
        sendQuickChat("안녕")
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.VOICE_CHANNEL_IN_USE));
    }

    @Test
    void 봇이_보낸_사람의_채널에_들어갈_수_없으면_403_과_bot_cannot_join_voice_channel() throws Exception {
        speak = command -> {
            throw new BotCannotJoinVoiceChannelException(new VoiceChannel(new GuildId(1L), 2L), null);
        };
        sendQuickChat("안녕")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.BOT_CANNOT_JOIN_VOICE_CHANNEL));
    }

    @Test
    void JSON_이_깨졌으면_400_과_invalid_request() throws Exception {
        mockMvc().perform(post(QuickTtsApi.QUICK_CHAT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.INVALID_REQUEST));
    }

    @Test
    void 예상하지_못한_실패면_500_과_unexpected_failure_이고_원인은_싣지_않는다() throws Exception {
        speak = command -> {
            throw new IllegalStateException("내부 사정");
        };
        sendQuickChat("안녕")
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.UNEXPECTED_FAILURE))
                .andExpect(jsonPath("$.detail").value(not(containsString("내부 사정"))));
    }

    @Test
    void 내_음성_채널에_없으면_204() throws Exception {
        mockMvc().perform(get(QuickTtsApi.MY_VOICE_CHANNEL_PATH))
                .andExpect(status().isNoContent());
    }

    private ResultActions sendQuickChat(String text) throws Exception {
        return mockMvc().perform(post(QuickTtsApi.QUICK_CHAT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"" + text + "\"}"));
    }

    private MockMvc mockMvc() {
        QuickChatController controller = new QuickChatController(speak, query -> Optional.empty());
        return MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }
}
